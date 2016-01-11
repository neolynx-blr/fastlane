package com.neolynks.vendor.manager;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import com.neolynks.vendor.manager.intf.VendorAdapter;
import com.neolynks.vendor.manager.test.TestVendorAdapter;
import com.neolynks.vendor.model.CurationConfig;
import com.neolynks.common.model.BaseResponse;
import com.neolynks.common.model.ErrorCode;
import com.neolynks.common.model.ItemMaster;
import com.neolynks.common.util.CSVWriter;

/**
 * Purpose of this file is to handle all inventory related operations locally on
 * vendor side machines.
 * 
 * Created by nitesh.garg on 02-Oct-2015
 */
@Slf4j
public class InventoryService {

	final CurationConfig curationConfig;
	final CSVWriter writer = new CSVWriter();
	final VendorAdapter adapter = new TestVendorAdapter();

	public InventoryService(CurationConfig curationConfig) {
		super();
		this.curationConfig = curationConfig;
	}

	// Load all data in CSV
	public BaseResponse generateInventoryMasterCSV() {

		BaseResponse response = new BaseResponse();
		List<ItemMaster> allInventory = adapter.getAllInventory();

		if (CollectionUtils.isNotEmpty(allInventory)) {

			log.info("Received [{}] records while extracting all vendor side inventory.", allInventory.size());
			List<String[]> allRecordsForLoad = new ArrayList<String[]>();

			for (ItemMaster record : allInventory) {
				allRecordsForLoad.add(record.generateCSVRecord());
			}

			int retryAttempts = 0;

			do {
				retryAttempts++;
				response = writer.writeInventoryRecords(this.curationConfig.getInventoryMasterFileName(),
						allRecordsForLoad);
			} while (response.getIsError() && retryAttempts < 3);

			if (!response.getIsError()) {
				log.debug("Successfully added [{}] records to the inventory-master CSV file [{}].",
						allRecordsForLoad.size(), this.curationConfig.getInventoryMasterFileNamePrint());
			} 
		} else {
			log.info("No records found while extracting all vendor side inventory, skipping the creation of CSV file.");
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().add(ErrorCode.MISSING_VENDOR_INVENTORY_DATA);
		}

		return response;
	}

}
