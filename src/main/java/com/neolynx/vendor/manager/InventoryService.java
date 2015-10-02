package com.neolynx.vendor.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynx.common.model.BaseResponse;
import com.neolynx.common.model.Error;
import com.neolynx.common.model.ItemMaster;
import com.neolynx.vendor.manager.intf.VendorAdapter;
import com.neolynx.vendor.manager.test.TestVendorAdapter;
import com.neolynx.vendor.model.CurationConfig;
import com.neolynx.vendor.util.CSVWriter;

/**
 * Purpose of this file is to handle all inventory related operations locally on
 * vendor side machines.
 * 
 * Created by nitesh.garg on 02-Oct-2015
 */

public class InventoryService {

	final CurationConfig curationConfig;
	final CSVWriter writer = new CSVWriter();
	final VendorAdapter adapter = new TestVendorAdapter();

	static Logger LOGGER = LoggerFactory.getLogger(InventoryService.class);

	public InventoryService(CurationConfig curationConfig) {
		super();
		this.curationConfig = curationConfig;
	}

	// Load all data in CSV
	public BaseResponse generateInventoryMasterCSV() {

		BaseResponse response = new BaseResponse();
		List<ItemMaster> allInventory = adapter.getAllInventory();

		if (CollectionUtils.isNotEmpty(allInventory)) {

			LOGGER.info("Received [{}] records while extracting all vendor side inventory.", allInventory.size());
			List<String[]> allRecordsForLoad = new ArrayList<String[]>();

			for (ItemMaster record : allInventory) {
				allRecordsForLoad.add(record.generateCSVRecord());
			}

			int retryAttempts = 0;
			List<Error> errorList = new ArrayList<Error>();

			do {
				retryAttempts++;
				errorList = writer.writeInventoryRecords(this.curationConfig.getInventoryMasterFileName(),
						allRecordsForLoad);
			} while (CollectionUtils.isNotEmpty(errorList) && retryAttempts < 3);

			if (CollectionUtils.isEmpty(errorList)) {
				LOGGER.debug("Successfully added [{}] records to the inventory-master CSV file [{}].",
						allRecordsForLoad.size(), this.curationConfig.getInventoryMasterFileNamePrint());

				response.setIsError(Boolean.FALSE);

			} else {

				response.setErrorDetails(errorList);

			}

		} else {

			LOGGER.info("No records found while extracting all vendor side inventory, skipping the creation of CSV file.");
			response.getErrorDetails().add(new Error("M001", "Missing data while generating inventory master data file for the vendor."));

		}

		return response;
	}

}
