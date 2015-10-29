package com.neolynx.curator.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynx.common.model.BaseResponse;
import com.neolynx.common.model.Error;
import com.neolynx.common.model.ErrorCode;
import com.neolynx.common.model.InventoryRequest;
import com.neolynx.common.model.ItemMaster;
import com.neolynx.common.model.ResponseAudit;
import com.neolynx.common.util.CSVReader;
import com.neolynx.curator.core.InventoryMaster;
import com.neolynx.curator.core.ProductMaster;
import com.neolynx.curator.core.VendorItemMaster;
import com.neolynx.curator.core.VendorVersionDetail;
import com.neolynx.curator.core.VendorVersionDifferential;
import com.neolynx.curator.db.InventoryMasterDAO;
import com.neolynx.vendor.model.CurationConfig;

/**
 * Created by nitesh.garg on 12-Sep-2015
 */
public class InventoryLoader {

	private final CacheCurator cacheCurator;
	private final CurationConfig curationConfig;
	private final InventoryMasterDAO invMasterDAO;

	final ProductMasterService pmService;
	final VendorItemService vendorItemService;
	final VendorVersionService vvDetailService;
	final VendorVersionDifferentialService vvDiffService;

	static Logger LOGGER = LoggerFactory.getLogger(InventoryLoader.class);

	public InventoryLoader(InventoryMasterDAO invMasterDAO, CurationConfig curationConfig, CacheCurator cacheCurator,
			ProductMasterService pmService, VendorVersionDifferentialService vvDiffService,
			VendorVersionService vvDetailService, VendorItemService vendorItemService) {
		super();
		this.invMasterDAO = invMasterDAO;
		this.cacheCurator = cacheCurator;
		this.curationConfig = curationConfig;

		this.pmService = pmService;
		this.vvDiffService = vvDiffService;
		this.vvDetailService = vvDetailService;
		this.vendorItemService = vendorItemService;
	}

	/**
	 * This is invoked to load fresh inventory of a vendor into the system by
	 * cleaning up anything existing stuff. TODO 1. Error handling 2. Ensure
	 * that sync process from client is not working in parallel 3. Make addition
	 * to history table as non critical operation as that should not potentially
	 * fail a critical operation 4. Ensure the load fresh is happening for one
	 * vendor at a time, because right now, the file names are generic which
	 * loads the fresh inventory
	 * 
	 * @param vendorId
	 * @return
	 */
	public BaseResponse freshInventoryLoad(Long vendorId) {

		final CSVReader reader = new CSVReader();
		BaseResponse response = new BaseResponse();
		List<CSVRecord> freshRecords = new ArrayList<CSVRecord>();

		freshRecords = reader.getAllPendingInventoryRecords(this.curationConfig.getInventoryMasterFileName());

		if (CollectionUtils.isEmpty(freshRecords)) {
			LOGGER.debug(
					"No records found while uploading fresh inventory for vendor [{}]. Skipping the operation and returning error.",
					vendorId);
			response.getErrorDetail().add(ErrorCode.MISSING_INVENTORY_FOR_LOAD);
			return response;
		}

		LOGGER.debug("[{}] records found while uploading fresh inventory for vendor [{}]", freshRecords.size(),
				vendorId);

		/**
		 * Before anything else, ensure that this vendor is completely cleaned
		 * up from the existing system, in case it already exists.
		 * 
		 * 1. Clean up vendor-version-differential data 2. Clean up
		 * vendor-version-detail 3. Clean up the product-master details 4. Move
		 * everything from vendor-item-master to history table 5. Clean up all
		 * the caches 6. Insert the new records in inventory-master 7. New entry
		 * for vendor-version-detail
		 * 
		 * Make multiple attempts to clear this up or else, give up without
		 * loading new inventory
		 */

		// #1
		this.vvDiffService.removeAllVersionDifferentialForVendor(vendorId);
		List<VendorVersionDifferential> vendorVersionDiffDetails = this.vvDiffService
				.getVersionDiffDetailsForVendor(vendorId);
		if (CollectionUtils.isNotEmpty(vendorVersionDiffDetails)) {
			LOGGER.debug(
					"Unable to remove version differentials for vendor [{}], still found [{}] pending entries. Skipping the fresh inventory load",
					vendorId, vendorVersionDiffDetails.size());
			response.getErrorDetail().add(ErrorCode.FAILED_CLEANING_VENDOR_DIFFERENTIAL_CACHE);
			return response;
		}

		// #2
		this.vvDetailService.removeAllVersionDetailsForVendor(vendorId);
		List<VendorVersionDetail> vendorVersionDetails = this.vvDetailService.getVersionDetailsForVendor(vendorId);
		if (CollectionUtils.isNotEmpty(vendorVersionDetails)) {
			LOGGER.debug(
					"Unable to remove version details for vendor [{}], still found [{}] pending entries. Skipping the fresh inventory load",
					vendorId, vendorVersionDetails.size());
			response.getErrorDetail().add(ErrorCode.FAILED_CLEANING_VENDOR_VERSION_CACHE);
			return response;
		}

		// #3
		this.pmService.removeVendorFromInventory(vendorId);
		List<ProductMaster> productMasterDetailsForVendor = this.pmService.getProductListForVendor(vendorId);
		if (CollectionUtils.isNotEmpty(productMasterDetailsForVendor)) {
			LOGGER.debug(
					"Unable to remove vendor [{}] from product-master details, still found [{}] pending entries. Skipping the fresh inventory load",
					vendorId, productMasterDetailsForVendor.size());
			response.getErrorDetail().add(ErrorCode.FAILED_CLEANING_VENDOR_PRODUCT_MASTER_RECORDS);
			return response;
		}

		// #4
		this.vendorItemService.removeAllItemRecordsForVendor(vendorId);
		List<VendorItemMaster> itemRecordsForVendor = this.vendorItemService.getAllItemRecordsForVendor(vendorId);
		if (CollectionUtils.isNotEmpty(itemRecordsForVendor)) {
			LOGGER.debug(
					"Unable to remove vendor [{}] from vendor-item-master details, still found [{}] pending entries. Skipping the fresh inventory load",
					vendorId, itemRecordsForVendor.size());
			response.getErrorDetail().add(ErrorCode.FAILED_CLEANING_VENDOR_ITEM_MASTER_RECORDS);
			return response;
		}

		// #5
		/**
		 * TODO Add some wait here to ensure this doesn't overlap with cache
		 * object being created in parallel
		 */

		this.cacheCurator.removeVersionCacheForVendor(vendorId);
		this.cacheCurator.removeDifferentialInventoryCache(vendorId);

		// #6
		List<ItemMaster> freshItemDetails = new ArrayList<ItemMaster>();
		for (CSVRecord record : freshRecords) {
			freshItemDetails.add(new ItemMaster(record));
		}
		addItemsToInventoryMaster(vendorId, freshItemDetails);

		// #7
		this.vvDetailService.createDefaultVendorVersionEntry(vendorId);

		response.setIsError(Boolean.FALSE);
		return response;

	}

	public ResponseAudit loadNewInventory(InventoryRequest request) {

		Long vendorId = request.getVendorId();
		Long dataVersionId = request.getDataVersionId();

		LOGGER.debug("Received request in loader for vendor-version [{}-{}]. RequestData:[{}]", vendorId,
				dataVersionId, request.getItemsUpdated().toString());

		ResponseAudit response = addItemsToInventoryMaster(vendorId, request.getItemsUpdated());
		return response;

	}

	private ResponseAudit addItemsToInventoryMaster(Long vendorId, List<ItemMaster> itemsToAdd) {

		ResponseAudit response = new ResponseAudit();

		for (ItemMaster instance : itemsToAdd) {

			List<InventoryMaster> existingRecord = this.invMasterDAO.getInventoryByUniqueConstraint(vendorId,
					instance.getVersionId(), instance.getItemCode(), instance.getBarcode());

			if (CollectionUtils.isEmpty(existingRecord)) {

				InventoryMaster inventoryMaster = new InventoryMaster();

				inventoryMaster.setVendorId(vendorId);
				inventoryMaster.setBarcode(instance.getBarcode());
				inventoryMaster.setItemCode(instance.getItemCode());
				inventoryMaster.setVersionId(instance.getVersionId());

				inventoryMaster.setCreatedOn(new java.sql.Date(System.currentTimeMillis()));
				inventoryMaster.setDescription(instance.getDescription());
				inventoryMaster.setDiscountJSON(instance.getDiscountJSON());
				inventoryMaster.setTaxJSON(instance.getTaxJSON());
				inventoryMaster.setImageJSON(instance.getImageJSON());
				inventoryMaster.setMrp(instance.getMrp());
				inventoryMaster.setPrice(instance.getPrice());
				inventoryMaster.setTagLine(instance.getTagline());
				inventoryMaster.setName(instance.getName());

				this.invMasterDAO.create(inventoryMaster);
				response.getSuccessIds().add(instance.getId());

			} else {

				response.getFailureIdCodeMap().put(instance.getId(), new Error("E0001", "Duplicate record"));

			}

		}

		response.setIsError(Boolean.FALSE);
		return response;
	}

}
