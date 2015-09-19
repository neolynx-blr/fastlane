package com.neolynx.curator.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynx.common.model.Error;
import com.neolynx.common.model.InventoryRequest;
import com.neolynx.common.model.ItemMaster;
import com.neolynx.common.model.ResponseAudit;
import com.neolynx.curator.client.HttpClientCustom;
import com.neolynx.curator.model.CurationConfig;
import com.neolynx.curator.util.CSVMapper;
import com.neolynx.curator.util.CSVReader;
import com.neolynx.curator.util.CSVWriter;
import com.neolynx.curator.util.Constant;

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

		final CSVReader reader = new CSVReader();
		final CSVWriter writer = new CSVWriter();

		final Adapter adapter = new Adapter();

		while (true) {

			try {

				Thread.sleep(15000);

				/*
				 * The processor class practically does the following, in order
				 * 1. Look for any new inventory data since the last sync time
				 * 2. Process the inventory CSV including the new data from #1
				 * and previous success id, and generate new file with
				 * to-be-updated data 3. Push the data from inventory files over
				 * to server now in chunks & record the success/error messages
				 */

				List<String> recentItemCodes = new ArrayList<String>();
				List<String[]> finalRecordsForLoad = new ArrayList<String[]>();

				String latestLastModifiedTimeStamp = adapter.getLatestLastModifiedBy();
				
				// Read the last sync identifier
				String lastSyncId = reader.getLastSyncIdentifier(this.curationConfig.getLastSyncIdFileName());

				//TODO lastSyncId is null?
				
				List<ItemMaster> recentRecords = null;
				if (this.curationConfig.getLastSyncIdType() == Integer.parseInt(lastSyncId)) {
					
					recentRecords = adapter.getRecentRecords(lastSyncId, latestLastModifiedTimeStamp);
					
				}

				LOGGER.debug(
						"Received [{}] new records from the vendor store. Looking for anything pending from last push to the server.",
						CollectionUtils.isEmpty(recentRecords) ? 0L : recentRecords.size());

				if (CollectionUtils.isNotEmpty(recentRecords)) {
					for (ItemMaster record : recentRecords) {
						recentItemCodes.add(record.getItemCode());
					}
				}

				/*
				 * Now that the latest data, since the last sync, has been
				 * picked up, mix this up with anything pending from last push
				 * and create the final master-inventory CSV file for further
				 * push over to the server.
				 */
				List<CSVRecord> pendingRecords = reader.getAllPendingInventoryRecords(this.curationConfig
						.getInventoryFileName());

				List<CSVRecord> lastSyncSuccessIds = reader.getLastSyncSuccessIds(this.curationConfig
						.getStatusFileName());

				LOGGER.debug(
						"Received [{}] records that were found on the CSV file, and [{}] entries for success-ids. now will remove the success ids from last push.",
						CollectionUtils.isEmpty(pendingRecords) ? 0L : pendingRecords.size(),
						CollectionUtils.isEmpty(lastSyncSuccessIds) ? 0L : lastSyncSuccessIds.size());

				/*
				 * First let's sort out everything about the pending records, if
				 * any, and if not marked successful last time.
				 */

				// If pending records from last sync
				if (CollectionUtils.isNotEmpty(pendingRecords)) {

					// Something was successful last time, so adjust
					if (CollectionUtils.isNotEmpty(lastSyncSuccessIds)) {

						// Get all the success-ids
						List<String> successIds = new ArrayList<String>();
						for (CSVRecord lastSyncSuccessId : lastSyncSuccessIds) {
							successIds.add(lastSyncSuccessId.get("id"));
						}

						/*
						 * Iterate over the pending records and add to the final
						 * list only if, 1. This id is not present in the
						 * success-ids list 2. This item-code is not having a
						 * new update in the recent records list
						 */
						for (CSVRecord pendingRecord : pendingRecords) {

							String recordId = pendingRecord.get("id");
							if (successIds.contains(recordId)) {
								LOGGER.debug(
										"Record with id [{}] from pending-records file was already successfully pushed, skipping...",
										recordId);
							} else {
								String itemCode = pendingRecord.get("item_code");
								if (recentItemCodes.contains(itemCode)) {
									LOGGER.debug(
											"Record with id [{}], item-code [{}] from pending-records file wasn't pushed (at all or successfully), but is not found again in the recent records. So skipping from pending records...",
											recordId, itemCode);
								} else {
									LOGGER.debug(
											"Record with id [{}], item-code [{}] from pending-records file wasn't pushed (at all or successfully), and not updatdin recent records. So adding...",
											recordId, itemCode);
									finalRecordsForLoad.add(CSVMapper.mapCSVRecordsToArray(pendingRecord,
											Constant.INVENTORY_FILE_HEADER));
								}
							}
						}

					} else {
						// And nothing was success (or wasn't tried) last time
						finalRecordsForLoad.addAll(CSVMapper.mapCSVRecordsToArrayList(pendingRecords,
								Constant.INVENTORY_FILE_HEADER));
					}

					LOGGER.debug(
							"Looking at the pending and success status files, retrieved [{}] old records for pushing to server.",
							finalRecordsForLoad.size());

				}

				/*
				 * Now that previous records are handled, look at the recently
				 * pulled records and if there are any, merge the 2 lists. Note
				 * that right now the data is being compared only on ItemCode
				 * but later we may need to move this comparison to include
				 * other parameters like bar-code. Marking it a TODO for now
				 */

				if (CollectionUtils.isNotEmpty(recentRecords)) {
					LOGGER.debug("Adding [{}] newly update records to the final list to be loaded", recentRecords.size());
					for (ItemMaster recentRecord : recentRecords) {
						finalRecordsForLoad.add(recentRecord.generateCSVRecord());
					}
				}

				/*
				 * In order to avoid any issues, first write all these records
				 * into the backup file. Then clear the main file, and write the
				 * records before deleting the backup file. Note that while
				 * reading data (code above), if main file is present, backup
				 * file will be ignored/removed to handle the case where back-up
				 * file deletion failed.
				 */

				// Write records to backup file
				int retryAttempts = 0;
				List<Error> backupWriterErrorList = writer.writeInventoryRecords(
						this.curationConfig.getBackupFileNameForInventory(), finalRecordsForLoad);
				
				while (CollectionUtils.isNotEmpty(backupWriterErrorList) && retryAttempts < 3) {
					
					retryAttempts++;
					LOGGER.debug("Attempt [{}] for loading records to backup file failed, trying again...", retryAttempts);
					
					backupWriterErrorList = writer.writeInventoryRecords(this.curationConfig.getBackupFileNameForInventory(), finalRecordsForLoad);
					
				}
				
				if (CollectionUtils.isEmpty(backupWriterErrorList)) {

					LOGGER.debug("Successfully added [{}] records to be loaded into the backup file. Generating main file and cleaning up the status/sync files.", finalRecordsForLoad.size());
					
					retryAttempts = 0;
					List<Error> mainWriterErrorList = writer.writeInventoryRecords(this.curationConfig.getInventoryFileName(), finalRecordsForLoad);
					while (CollectionUtils.isNotEmpty(mainWriterErrorList) && retryAttempts < 3) {
						
						retryAttempts++;
						LOGGER.debug("Attempt [{}] for loading records to main inventory file failed, trying again...", retryAttempts);
						
						mainWriterErrorList = writer.writeInventoryRecords(this.curationConfig.getInventoryFileName(), finalRecordsForLoad);
						
					}

					
					if (CollectionUtils.isEmpty(mainWriterErrorList)) {

						LOGGER.debug("Successfully added [{}] records to the main file. Cleaning up the backup/status/sync files.", finalRecordsForLoad.size());
						writer.clearFileContents(this.curationConfig.getBackupFileNameForInventory(), Constant.INVENTORY_FILE_HEADER);

					}
					
					LOGGER.debug("Updating the last sync time to [{}] ...", latestLastModifiedTimeStamp);
					writer.writeLastSyncIdentifierRecord(this.curationConfig.getLastSyncIdFileName(), latestLastModifiedTimeStamp);

					LOGGER.debug("Clearing the last status file ...", latestLastModifiedTimeStamp);
					writer.clearFileContents(this.curationConfig.getStatusFileName(), Constant.STATUS_FILE_HEADER);


				} else {
					
					/*
					 * Already tried multiple attempts, so for now, skip the
					 * work flow and next attempt will be made after configured
					 * delay.
					 */

					LOGGER.debug(
							"Unable to create backlog file with new set of [{}] records, skipping this and moving on see if any pending records can be processed instead.",
							finalRecordsForLoad.size());
					
				}

				/*
				 * Just a gap which actually has no real need/meaning but by now
				 * the new data points pulled from vendor inventory are merged
				 * with previous data (if any was pending) and new file has been
				 * created which now contains the true delta that needs to be
				 * pushed to server. Even if the new loader failed for some
				 * reason, rest of the flow will look at the previous pending
				 * records, if any, and get those processed in the mean time.
				 */
				Thread.sleep(2000);

				LOGGER.debug("About to call CSVReader to read and return data from [{}]", this.curationConfig.getInventoryFileName());
				List<CSVRecord> records = reader.getAllPendingInventoryRecords(this.curationConfig.getInventoryFileName());
				LOGGER.debug("Received [{}] records to be pushed to server.", CollectionUtils.isEmpty(records) ? 0L : records.size());

				if (CollectionUtils.isNotEmpty(records)) {

					List<Long> successIds = new ArrayList<Long>();

					InventoryRequest request = new InventoryRequest();
					request.setItemsUpdated(new ArrayList<ItemMaster>());
					request.setVendorId(this.curationConfig.getVendorId());

					Iterator<CSVRecord> recordIterator = records.iterator();
					
					// Skipping the header row
					recordIterator.next();
					
					while (recordIterator.hasNext()) {

						CSVRecord record = recordIterator.next();

						ItemMaster itemDetail = new ItemMaster(record);
						request.getItemsUpdated().add(itemDetail);

						if (request.getItemsUpdated().size() == this.curationConfig.getMaxRowCountForServerPost()) {
							
							HttpClientCustom client = new HttpClientCustom();
							ResponseAudit response = client.postData(request);
							
							LOGGER.debug("Sent chunk of [{}] inventory records, and received [{}] success and [{}] failures.",
									request.getItemsUpdated().size(), response.getSuccessIds().size(), response.getFailureIdCodeMap().size());

							successIds.addAll(response.getSuccessIds());
							request.setItemsUpdated(new ArrayList<ItemMaster>());
						}

					}

					if (request.getItemsUpdated().size() > 0) {
						HttpClientCustom client = new HttpClientCustom();
						ResponseAudit response = client.postData(request);
						
						LOGGER.debug("Sent chunk of [{}] inventory records, and received [{}] success and [{}] failures.",
								request.getItemsUpdated().size(), response.getSuccessIds().size(), response.getFailureIdCodeMap().size());

						successIds.addAll(response.getSuccessIds());
					}

					// If everything went fine, cleanup inventory and status files.
					if(successIds.size() == finalRecordsForLoad.size()) {
						
						LOGGER.debug("Since all records were successfully pushed,");
						LOGGER.debug("Cleaning up the main inventory file...");
						writer.clearFileContents(this.curationConfig.getInventoryFileName(), Constant.INVENTORY_FILE_HEADER);
						LOGGER.debug("Cleaning up the status file...");
						writer.clearFileContents(this.curationConfig.getStatusFileName(), Constant.STATUS_FILE_HEADER);
						LOGGER.debug("Completed.");
						
					} else {
						LOGGER.debug("Since all records were not successfully pushed, [{}] out of [{}], updating the status file.", successIds.size(), finalRecordsForLoad.size());
						writer.writeLoadStatusRecords(this.curationConfig.getStatusFileName(), successIds);
					}

				}

			} catch (InterruptedException ie) {
			}

		}

	}

}
