package com.neolynx.vendor.manager.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.neolynx.common.model.ItemMaster;
import com.neolynx.vendor.manager.intf.VendorAdapter;

/**
 * Created by nitesh.garg on 21-Sep-2015
 */
public class TestVendorAdapter implements VendorAdapter {

	public TestVendorAdapter() {

		// 1st Set
		ItemMaster item = new ItemMaster(null, 1234567890L, "ICode001", null, "IName001-1", "ITagLine001-1",
				"IDesc001-1", 80.91D, 79.90D, null, null, null, new Date(System.currentTimeMillis()));
		itemList.add(item);
		
		// 2nd Set
		item = new ItemMaster(null, 1234567890L, "ICode001", null, "IName001-2", "ITagLine001-1",
				"IDesc001-1", 80.91D, 79.90D, null, null, null, new Date(System.currentTimeMillis()));
		itemList.add(item);
		
		
		// 3rd Set
		item = new ItemMaster(null, 1234567890L, "ICode001", null, "IName001-3", "ITagLine001-2",
				"IDesc001-2", 80.91D, 79.90D, null, null, null, new Date(System.currentTimeMillis()));
		itemList.add(item);
		
		
		
		// 4th Set
		item = new ItemMaster(null, 1234567890L, "ICode001", null, "IName001-3", "ITagLine001-2",
				"IDesc001-2", 90.91D, 89.90D, null, null, null, new Date(System.currentTimeMillis()));
		itemList.add(item);
		
		
		// 5th Set
		item = new ItemMaster(null, 1234567890L, "ICode001", null, "IName001-3", "ITagLine001-2",
				"IDesc001-3", 99.91D, 99.90D, null, null, null, new Date(System.currentTimeMillis()));
		itemList.add(item);
		
	}

	public int attemptCount = 0;
	public List<ItemMaster> itemList = new ArrayList<ItemMaster>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.neolynx.curator.manager.VendorAdapter#getLatestLastModifiedBy()
	 */
	@Override
	public String getLatestInventoryTimestamp() {
		return String.valueOf(attemptCount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.neolynx.curator.manager.VendorAdapter#getRecentRecords(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public List<ItemMaster> getInventoryUpdateInTimeRange(String startTimeStamp, String endTimeStamp) {
		
		List<ItemMaster> response = new ArrayList<ItemMaster>();
		if(attemptCount < 5)
			response.add(itemList.get(attemptCount++));
		
		return response;
	}

	/* (non-Javadoc)
	 * @see com.neolynx.curator.manager.VendorAdapter#generateCompleteInventorySet()
	 */
	@Override
	public List<ItemMaster> getAllInventory() {
		List<ItemMaster> response = new ArrayList<ItemMaster>();
		response.add(itemList.get(0));
		return response;
	}

}
