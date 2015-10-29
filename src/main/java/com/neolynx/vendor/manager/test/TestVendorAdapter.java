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
		ItemMaster item = new ItemMaster(null, "1234567890", "ICode001", null, "IName001-1", "ITagLine001-1",
				"IDesc001-1", 80.91D, 79.90D, null, null, null, new Date(System.currentTimeMillis()));
		itemList.add(item);
		
		// 2nd Set
		item = new ItemMaster(null, "1234567890", "ICode001", null, "IName001-2", "ITagLine001-1",
				"IDesc001-1", 80.91D, 79.90D, null, null, null, new Date(System.currentTimeMillis()));
		itemList.add(item);
		
		
		// 3rd Set
		item = new ItemMaster(null, "1234567890", "ICode001", null, "IName001-3", "ITagLine001-2",
				"IDesc001-2", 80.91D, 79.90D, null, null, null, new Date(System.currentTimeMillis()));
		itemList.add(item);
		
		
		
		// 4th Set
		item = new ItemMaster(null, "1234567890", "ICode001", null, "IName001-3", "ITagLine001-2",
				"IDesc001-2", 90.91D, 89.90D, null, null, null, new Date(System.currentTimeMillis()));
		itemList.add(item);
		
		
		// 5th Set
		item = new ItemMaster(null, "1234567890", "ICode001", null, "IName001-3", "ITagLine001-2",
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
		response.add(itemList.get(itemList.size()-1));
		response.add(new ItemMaster(null, "1234567892", "ICode002", null, "IName002-3", "ITagLine002-2",
				"IDesc002-3", 92.91D, 92.90D, null, null, null, new Date(System.currentTimeMillis())));
		response.add(new ItemMaster(null, "1234567893", "ICode003", null, "IName003-3", "ITagLine003-2",
				"IDesc003-3", 93.91D, 93.90D, null, null, null, new Date(System.currentTimeMillis())));
		response.add(new ItemMaster(null, "1234567894", "ICode004", null, "IName004-3", "ITagLine004-2",
				"IDesc004-3", 94.91D, 94.90D, null, null, null, new Date(System.currentTimeMillis())));
		response.add(new ItemMaster(null, "1234567895", "ICode005", null, "IName005-3", "ITagLine005-2",
				"IDesc005-3", 95.91D, 95.90D, null, null, null, new Date(System.currentTimeMillis())));
		return response;
	}

}
