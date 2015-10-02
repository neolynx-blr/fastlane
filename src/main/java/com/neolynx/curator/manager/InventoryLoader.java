package com.neolynx.curator.manager;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynx.common.model.Error;
import com.neolynx.common.model.InventoryRequest;
import com.neolynx.common.model.ItemMaster;
import com.neolynx.common.model.ResponseAudit;
import com.neolynx.curator.core.InventoryMaster;
import com.neolynx.curator.db.InventoryMasterDAO;

/**
 * Created by nitesh.garg on 12-Sep-2015
 */
public class InventoryLoader {

	private final InventoryMasterDAO invMasterDAO;
	static Logger LOGGER = LoggerFactory.getLogger(InventoryLoader.class);

	public InventoryLoader(InventoryMasterDAO invMasterDAO) {
		super();
		this.invMasterDAO = invMasterDAO;
	}

	public ResponseAudit loadNewInventory(InventoryRequest request) {

		Long vendorId = request.getVendorId();
		Long dataVersionId = request.getDataVersionId();

		LOGGER.debug("Received request in loader for vendor-version [{}-{}]. RequestData:[{}]", vendorId,
				dataVersionId, request.getItemsUpdated().toString());

		ResponseAudit response = new ResponseAudit();

		for (ItemMaster instance : request.getItemsUpdated()) {

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
