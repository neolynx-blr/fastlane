package com.neolynx.vendor.manager;

import java.util.List;

import com.neolynx.common.model.ItemMaster;
import com.neolynx.vendor.manager.intf.VendorAdapter;

/**
 * This is the adapter class which will be specific to a vendor and will
 * actually pull the recent data based on last sync time. To ensure consistent
 * behavior along side rest of the classes, this must implement the given
 * interface.
 * 
 * Created by nitesh.garg on 18-Sep-2015
 */
public class Adapter implements VendorAdapter {

	/* (non-Javadoc)
	 * @see com.neolynx.curator.manager.VendorAdapter#getLatestLastModifiedBy()
	 */
	@Override
	public String getLatestInventoryTimestamp() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.neolynx.curator.manager.VendorAdapter#getRecentRecords(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ItemMaster> getInventoryUpdateInTimeRange(String startTimeStamp, String endTimeStamp) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.neolynx.curator.manager.VendorAdapter#generateCompleteInventorySet()
	 */
	@Override
	public List<ItemMaster> getAllInventory() {
		// TODO Auto-generated method stub
		return null;
	}

}
