package com.neolynx.curator.manager;

import java.util.List;

import com.neolynx.common.model.ItemMaster;

/**
 * Defines the core functions every vendor specific adapter must implement.
 * 
 * Created by nitesh.garg on 18-Sep-2015
 */
public interface VendorAdapter {
	
	/*
	 * This function is invoked to get the latest last-modified-on like
	 * construct from the vendor inventory. The idea being that, while
	 * refreshing the inventory, this will be invoked first and later the actual
	 * data will be pulled till this timestamp - 1. This save "some" corner
	 * cases when data is still coming into vendor inventory store with the same
	 * timestamp and consumer assumes that last sync time can be updated to this
	 * timestamp.
	 */
	String getLatestLastModifiedBy();

	/*
	 * Pull the data updated in between the two timestamps now. This would
	 * generally be the last sync time and last know last-modified-by as
	 * returned by the function "getLatestLastModifiedBy" above. Currently the
	 * timestamp string is assumed to be system time in milli-seconds
	 */
	List<ItemMaster> getRecentRecords(String startTimeStamp, String endTimeStamp);

}
