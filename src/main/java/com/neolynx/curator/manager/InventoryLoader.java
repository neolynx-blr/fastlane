package com.neolynx.curator.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynx.common.model.Error;
import com.neolynx.common.model.InventoryRequest;
import com.neolynx.common.model.ItemMaster;
import com.neolynx.common.model.ResponseAudit;
import com.neolynx.common.util.CSVReader;
import com.neolynx.curator.core.InventoryMaster;
import com.neolynx.curator.core.VendorVersionDetail;
import com.neolynx.curator.core.VendorVersionDifferential;
import com.neolynx.curator.db.InventoryMasterDAO;
import com.neolynx.curator.db.VendorVersionDetailDAO;
import com.neolynx.curator.db.VendorVersionDifferentialDAO;
import com.neolynx.vendor.model.CurationConfig;

/**
 * Created by nitesh.garg on 12-Sep-2015
 */
public class InventoryLoader {

	private final CacheCurator cacheCurator;
	private final CurationConfig curationConfig;
	private final InventoryMasterDAO invMasterDAO;
	private final VendorVersionDetailDAO vendorVersionDetailDAO;
	private final VendorVersionDifferentialDAO vendorVersionDifferentialDAO;

	static Logger LOGGER = LoggerFactory.getLogger(InventoryLoader.class);

	public InventoryLoader(InventoryMasterDAO invMasterDAO, VendorVersionDetailDAO vendorVersionDetailDAO,
			VendorVersionDifferentialDAO vendorVersionDifferentialDAO, CurationConfig curationConfig,
			CacheCurator cacheCurator) {
		super();
		this.invMasterDAO = invMasterDAO;
		this.cacheCurator = cacheCurator;
		this.curationConfig = curationConfig;
		this.vendorVersionDetailDAO = vendorVersionDetailDAO;
		this.vendorVersionDifferentialDAO = vendorVersionDifferentialDAO;
	}

	public ResponseAudit freshInventoryLoad(Long vendorId) {

		final CSVReader reader = new CSVReader();
		ResponseAudit response = new ResponseAudit();
		List<CSVRecord> freshRecords = new ArrayList<CSVRecord>();

		freshRecords = reader.getAllPendingInventoryRecords(this.curationConfig.getInventoryMasterFileName());

		if (CollectionUtils.isNotEmpty(freshRecords)) {

			LOGGER.debug("[{}] records found while uploading fresh inventory for vendor [{}]", freshRecords.size(),
					vendorId);
			List<ItemMaster> freshItemDetails = new ArrayList<ItemMaster>();

			for (CSVRecord record : freshRecords) {
				freshItemDetails.add(new ItemMaster(record));
			}

			/*
			 * TODO For now assume this will only be called for a new vendor
			 * only
			 * 
			 * Before you add up fresh inventory for the vendor, you'll need to
			 * remove all previous details for this vendor include cache entries
			 * and different data versions.
			 */

			this.vendorVersionDetailDAO.deleteByVendorId(vendorId);
			List<VendorVersionDetail> vendorVersionDetails = this.vendorVersionDetailDAO.findByVendor(vendorId);
			
			this.vendorVersionDifferentialDAO.deleteByVendorId(vendorId);
			List<VendorVersionDifferential> vendorVersionDifferentialDetails = this.vendorVersionDifferentialDAO.findByVendor(vendorId);

			if (CollectionUtils.isEmpty(vendorVersionDetails) && CollectionUtils.isEmpty(vendorVersionDifferentialDetails)) {

				/**
				 * TODO Add some wait here to ensure this doesn't overlap with
				 * cache object being created in parallel
				 */
				
				// Remove from differential cache
				this.cacheCurator.removeDifferentialInventoryCache(vendorId);

				response = addItemsToInventoryMaster(vendorId, freshItemDetails);
				
				VendorVersionDetail newVendorVersionDetail = new VendorVersionDetail();
				newVendorVersionDetail.setVendorId(vendorId);
				newVendorVersionDetail.setLatestSyncedVersionId(0L);
				newVendorVersionDetail.setLastModifiedOn(new Date(System.currentTimeMillis()));
				
				this.vendorVersionDetailDAO.create(newVendorVersionDetail);
				
			} else {

			}

		} else {

			LOGGER.debug("No records found while uploading fresh inventory for vendor [{}]. Skipping.", vendorId);

		}

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
				inventoryMaster.setDiscountType(instance.getDiscountType());
				inventoryMaster.setDiscountValue(instance.getDiscountValue());
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
