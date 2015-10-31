package com.neolynx.vendor.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynx.common.model.BaseResponse;
import com.neolynx.common.model.Error;
import com.neolynx.common.model.InventoryRequest;
import com.neolynx.common.model.ItemMaster;
import com.neolynx.common.model.ResponseAudit;
import com.neolynx.common.util.CSVMapper;
import com.neolynx.common.util.CSVReader;
import com.neolynx.common.util.CSVWriter;
import com.neolynx.common.util.Constant;
import com.neolynx.vendor.client.HttpClientCustom;
import com.neolynx.vendor.manager.test.TestVendorAdapter;
import com.neolynx.vendor.model.CurationConfig;

/**
 * Created by nitesh.garg on 17-Sep-2015
 */
public class InventorySyncCron implements Runnable {

	final CurationConfig curationConfig;
	static Logger LOGGER = LoggerFactory.getLogger(InventorySyncCron.class);

	public InventorySyncCron(CurationConfig curationConfig) {
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

		final TestVendorAdapter adapter = new TestVendorAdapter();

		while (true) {

			try {

				Thread.sleep(45000);

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

				String latestLastModifiedTimeStamp = adapter.getLatestInventoryTimestamp();
				
				// Read the last sync identifier
				String lastSyncId = reader.getLastSyncIdentifier(this.curationConfig.getLastSyncIdFileName());
				
				List<ItemMaster> recentRecords = null;
				if(lastSyncId == null) {
					/*
					 * TODO lastSyncId is null? It basically indicated that the
					 * file got corrupted or the vendor is here for the first
					 * time. For first time vendors, we should have an offline
					 * provision to enter first time inventory and set this file
					 * somehow. But if the file is lost, let's have a call from
					 * server to indicate when is the last data point received
					 * from this vendor and adjust accordingly.
					 */
					
					
					
				}
				
				/*
				 * For now, in case of handling null lastSyncId, basically pull
				 * everything from scratch. Although this should be avoided and
				 * rather pulled offline or some optimized way.
				 */
				if (this.curationConfig.getLastSyncIdType() == Constant.VENDOR_DATA_SYNC_ID_TIMESTAMP_MILLIS) {
					recentRecords = adapter.getInventoryUpdateInTimeRange(lastSyncId, latestLastModifiedTimeStamp);
				} 

				LOGGER.info(
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
				List<CSVRecord> pendingRecords = new ArrayList<CSVRecord>();
				List<CSVRecord> lastSyncSuccessIds = reader.getLastSyncSuccessIds(this.curationConfig
						.getStatusFileName());
				
				
				/*
				 * Handing scenarios with these files.
				 * 1. Sync file should always be populated unless first time vendor or file corruption
				 * 2. Status file will not be there when 
				 * 	a. Either the last push to server was ALL successful, nothing pending
				 *  b. If not empty, backup file has the last accumulated data and should be worked upon
				 *  c. If backup file is empty, and main file is not empty, it has the last accumulated data and should be worked upon
				 */
				
				if(CollectionUtils.isEmpty(lastSyncSuccessIds)) {
					pendingRecords = reader.getAllPendingInventoryRecords(this.curationConfig.getBackupFileNameForInventory());
					if(CollectionUtils.isEmpty(pendingRecords)) {
						pendingRecords = reader.getAllPendingInventoryRecords(this.curationConfig.getInventoryFileName());
					} else {
						LOGGER.debug("Received [{}] records from the backup file as status file was empty.", pendingRecords.size());
					}
				} 

				LOGGER.info(
						"Received [{}] records that were found on the CSV file, and [{}] entries for success-ids.",
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
											"Record with id [{}], item-code [{}] from pending-records file wasn't pushed (at all or successfully), but is now found again in the recent records. So skipping from pending records...",
											recordId, itemCode);
								} else {
									LOGGER.debug(
											"Record with id [{}], item-code [{}] from pending-records file wasn't pushed (at all or successfully), and not updated in recent records. So adding...",
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

					LOGGER.info(
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
				BaseResponse backupWriterErrorList = writer.writeInventoryRecords(
						this.curationConfig.getBackupFileNameForInventory(), finalRecordsForLoad);
				
				while (backupWriterErrorList.getIsError() && retryAttempts < 3) {
					
					retryAttempts++;
					LOGGER.debug("Attempt [{}] for loading records to backup file failed, trying again...", retryAttempts);
					
					backupWriterErrorList = writer.writeInventoryRecords(this.curationConfig.getBackupFileNameForInventory(), finalRecordsForLoad);
					
				}
				
				if (!backupWriterErrorList.getIsError()) {

					LOGGER.debug("Successfully added [{}] records to be loaded into the backup file. Generating main file and cleaning up the status/sync files.", finalRecordsForLoad.size());
					
					retryAttempts = 0;
					BaseResponse mainWriterErrorList = writer.writeInventoryRecords(this.curationConfig.getInventoryFileName(), finalRecordsForLoad);
					while (mainWriterErrorList.getIsError() && retryAttempts < 3) {
						
						retryAttempts++;
						LOGGER.debug("Attempt [{}] for loading records to main inventory file failed, trying again...", retryAttempts);
						
						mainWriterErrorList = writer.writeInventoryRecords(this.curationConfig.getInventoryFileName(), finalRecordsForLoad);
						
					}

					
					if (!mainWriterErrorList.getIsError()) {

						LOGGER.debug("Successfully added [{}] records to the main file. Cleaning up the backup/status/sync files.", finalRecordsForLoad.size());
						writer.clearFileContents(this.curationConfig.getBackupFileNameForInventory(), Constant.INVENTORY_FILE_HEADER);

					} else {
						//Try cleaning the main file
						mainWriterErrorList = writer.clearFileContents(this.curationConfig.getInventoryFileName(), Constant.INVENTORY_FILE_HEADER);
					}

					if(!mainWriterErrorList.getIsError()) {
						LOGGER.debug("Clearing the last status file ...", latestLastModifiedTimeStamp);
						BaseResponse statusWriterErrorList = writer.clearFileContents(this.curationConfig.getStatusFileName(), Constant.STATUS_FILE_HEADER);

						if(!statusWriterErrorList.getIsError()) {
							LOGGER.debug("Updating the last sync time to [{}] ...", latestLastModifiedTimeStamp);
							BaseResponse syncWriterErrorList = writer.writeLastSyncIdentifierRecord(this.curationConfig.getLastSyncIdFileName(), latestLastModifiedTimeStamp);
							
							if(!syncWriterErrorList.getIsError()) {
								LOGGER.debug("All files updated successfully.");
							}
						}
					} else {
						/*
						 * If main file is not corrected, let things be
						 * reprocessed next time. Only issue is that what if
						 * main file update failed in the middle. In that case,
						 * re-processing may end up considering half baked main
						 * file next time.
						 */
					}




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

				LOGGER.debug("About to call CSVReader to read and return data from [{}]", this.curationConfig.getInventoryFileNamePrint());
				List<CSVRecord> records = reader.getAllPendingInventoryRecords(this.curationConfig.getInventoryFileName());
				LOGGER.info("Received [{}] records to be pushed to server from [{}].", CollectionUtils.isEmpty(records) ? 0L : records.size(), this.curationConfig.getInventoryFileNamePrint());

				if (CollectionUtils.isNotEmpty(records)) {

					List<Long> successIds = new ArrayList<Long>();
					HttpClientCustom client = new HttpClientCustom(this.curationConfig);
					
					InventoryRequest request = new InventoryRequest();
					request.setItemsUpdated(new ArrayList<ItemMaster>());
					request.setVendorId(this.curationConfig.getVendorId());

					Iterator<CSVRecord> recordIterator = records.iterator();
					
					while (recordIterator.hasNext()) {

						CSVRecord record = recordIterator.next();

						ItemMaster itemDetail = new ItemMaster(record);
						request.getItemsUpdated().add(itemDetail);

						if (request.getItemsUpdated().size() == this.curationConfig.getMaxRowCountForServerPost()) {
							
							
							ResponseAudit response = client.postData(request);
							
							LOGGER.info("Sent chunk of [{}] inventory records, and received [{}] success and [{}] failures.",
									request.getItemsUpdated().size(), response.getSuccessIds().size(), response.getFailureIdCodeMap().size());

							successIds.addAll(response.getSuccessIds());
							request.setItemsUpdated(new ArrayList<ItemMaster>());
						}

					}

					if (request.getItemsUpdated().size() > 0) {

						ResponseAudit response = client.postData(request);
						
						LOGGER.info("Sent chunk of [{}] inventory records, and received [{}] success and [{}] failures.",
								request.getItemsUpdated().size(), response.getSuccessIds().size(), response.getFailureIdCodeMap().size());

						successIds.addAll(response.getSuccessIds());
						
						// Add duplicates to success as well
						for(Long key : response.getFailureIdCodeMap().keySet()) {
							Error errorDetail = response.getFailureIdCodeMap().get(key);
							if(errorDetail.getErrorCode().equalsIgnoreCase("E0001")) {
								successIds.add(key);
							}
						}
						
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
