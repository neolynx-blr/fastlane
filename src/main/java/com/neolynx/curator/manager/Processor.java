package com.neolynx.curator.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynx.common.model.InventoryRequest;
import com.neolynx.common.model.ItemMaster;
import com.neolynx.common.model.ResponseAudit;
import com.neolynx.curator.client.HttpClientCustom;
import com.neolynx.curator.model.CurationConfig;
import com.neolynx.curator.util.CSVReader;
import com.neolynx.curator.util.CSVWriter;

/**
 * Created by nitesh.garg on 17-Sep-2015
 */
public class Processor implements Runnable {

	final CurationConfig curationConfig;
	static Logger LOGGER = LoggerFactory.getLogger(Processor.class);

	public Processor(CurationConfig curationConfig) {
		super();
		this.curationConfig = curationConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		CSVReader reader = new CSVReader();
		while (true) {

			try {

				Thread.sleep(15000);

				LOGGER.debug("About to call CSVReader to read and return data from [{}]",
						this.curationConfig.getInventoryFileName());
				List<CSVRecord> records = reader.getAllPendingRecords(this.curationConfig.getInventoryFileName());
				LOGGER.debug("Received [{}] rcords.", CollectionUtils.isEmpty(records) ? 0L : records.size());

				if (CollectionUtils.isNotEmpty(records)) {
					
					List<Long> successIds = new ArrayList<Long>();

					InventoryRequest request = new InventoryRequest();
					request.setItemsUpdated(new ArrayList<ItemMaster>());
					request.setVendorId(this.curationConfig.getVendorId());

					Iterator<CSVRecord> recordIterator = records.iterator();
					//Skipping the header row
					recordIterator.next();
					while (recordIterator.hasNext()) {

						CSVRecord record = recordIterator.next();

						ItemMaster itemDetail = new ItemMaster();

						itemDetail.setId(Long.parseLong(record.get("id")));
						itemDetail.setVersionId(Long.parseLong(record.get("version_id")));
						itemDetail.setItemCode(record.get("item_code"));
						itemDetail.setBarcode(Long.parseLong(record.get("barcode")));

						itemDetail.setName(record.get("name"));
						itemDetail.setDescription(record.get("description"));
						itemDetail.setTagline(record.get("tag_line"));

						itemDetail.setImageJSON(record.get("image_json"));

						itemDetail.setMrp(Double.parseDouble(record.get("mrp")));
						itemDetail.setPrice(Double.parseDouble(record.get("price")));
						itemDetail.setDiscountType(Integer.parseInt(record.get("discount_type")));
						itemDetail.setDiscountValue(Double.parseDouble(record.get("discount_value")));

						request.getItemsUpdated().add(itemDetail);

						if (request.getItemsUpdated().size() == this.curationConfig.getMaxRowCountForServerPost()) {
							HttpClientCustom client = new HttpClientCustom();
							ResponseAudit response = client.postData(request);

							successIds.addAll(response.getSuccessIds());
							
							request.setItemsUpdated(new ArrayList<ItemMaster>());
							
						}

					}

					if (request.getItemsUpdated().size() > 0) {
						HttpClientCustom client = new HttpClientCustom();
						ResponseAudit response = client.postData(request);
						
						successIds.addAll(response.getSuccessIds());
					}

					CSVWriter writer = new CSVWriter();
					writer.setPostStatus(this.curationConfig.getStatusFileName(), successIds);
					
					writer.createNewEmptyFile(this.curationConfig.getInventoryFileName());

				}

			} catch (InterruptedException ie) {

			}

		}

	}

}
