package com.neolynks.vendor.manager.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.neolynks.vendor.manager.intf.VendorAdapter;
import com.neolynks.common.model.ItemMaster;

/**
 * Created by nitesh.garg on 21-Sep-2015
 */
public class TestVendorAdapter implements VendorAdapter {

	public TestVendorAdapter() {
	}

	public int attemptCount = 0;
	public List<ItemMaster> itemList = new ArrayList<ItemMaster>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.neolynks.curator.manager.VendorAdapter#getLatestLastModifiedBy()
	 */
	@Override
	public String getLatestInventoryTimestamp() {
		return String.valueOf(attemptCount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.neolynks.curator.manager.VendorAdapter#getRecentRecords(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public List<ItemMaster> getInventoryUpdateInTimeRange(String startTimeStamp, String endTimeStamp) {
		
		return new ArrayList<ItemMaster>();
		/*
		if(attemptCount == 0) {
			List<ItemMaster> iml = new ArrayList<ItemMaster>();
			iml.add(new ItemMaster(null, "1234567890", "ICode001", null, "IName001-1", "ITagLine001-1",
					"IDesc001-1", 80.91D, 79.90D, null, null, null, new Date(System.currentTimeMillis())));
			return iml;
		} else if(attemptCount == 1) {
			List<ItemMaster> iml = new ArrayList<ItemMaster>();
			iml.add(new ItemMaster(null, "1234567890", "ICode001", null, "IName001-2", "ITagLine001-1",
					"IDesc001-1", 80.91D, 79.90D, null, null, null, new Date(System.currentTimeMillis())));
			return iml;			
		} else if(attemptCount == 2) {
			List<ItemMaster> iml = new ArrayList<ItemMaster>();
			iml.add(new ItemMaster(null, "1234567890", "ICode001", null, "IName001-3", "ITagLine001-3",
					"IDesc001-3", 80.91D, 79.90D, null, null, null, new Date(System.currentTimeMillis())));
			return iml;
		} else if(attemptCount == 3) {
			List<ItemMaster> iml = new ArrayList<ItemMaster>();
			iml.add(new ItemMaster(null, "1234567890", "ICode001", null, "IName001-3", "ITagLine001-3",
					"IDesc001-3", 90.91D, 89.90D, null, null, null, new Date(System.currentTimeMillis())));
			return iml;
		} else if(attemptCount == 4) {
			List<ItemMaster> iml = new ArrayList<ItemMaster>();
			iml.add(new ItemMaster(null, "1234567890", "ICode001", null, "IName001-4", "ITagLine001-4",
					"IDesc001-4", 99.91D, 99.90D, null, null, null, new Date(System.currentTimeMillis())));
			return iml;
		}
/*

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0001',1,'X','X-Description','X-Tagline', '1234', 1.2, 1.0,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0001',2,'X2','X-Description','X-Tagline', '1234', 1.3, 1.1,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0001',3,'X3','X3-Description','X3-Tagline', '1234', 1.3, 1.1,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0001',4,'X3','X3-Description','X3-Tagline', '1234', 2.3, 2.1,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0001',5,'X4','X4-Description','X4-Tagline', '1234', 3.3, 3.1,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0002',6,'Y4','Y4-Description','Y4-Tagline', '1235', 13.3, 13.1,  'Y-JSON', now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0003',7,'Z4','Z4-Description','Z4-Tagline', '1236', 23.3, 23.1,  'Z-JSON', now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0004',8,'A4','B4-Description','B4-Tagline', '1237', 33.3, 33.1,  'A-JSON', now());

*******************************************************************************************************************************

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0005',8,'B4','B4-Description','B4-Tagline', '1238', 43.3, 43.1,  'B-JSON', now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0001',8,'X3New','X3New-Description','X3New-Tagline', '1234', 111.3, 111.1,  'X3New-JSON', now());


insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0004',9,'A4','A4-Description','A4-Tagline', '1237', 33.33, 33.11,  'A-JSON', now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0006',9,'C4','C4-Description','C4-Tagline', '1239', 53.3, 53.1,  'C-JSON', now());

*******************************************************************************************************************************

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0001',9,'X3New2','X3New2-Description','X3New2-Tagline', '1234', 111.32, 111.12,  'X3New2-JSON', now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0005',9,'B4New','B4New-Description','B4-Tagline', '1238', 43.34, 43.14,  'B-JSON', now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0001',10,'X3New3','X3New3-Description','X3New2-Tagline', '1234', 111.32, 111.12,  'X3New2-JSON', now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0005',10,'B4New4','B4New-Description','B4-Tagline', '1238', 43.34, 43.14,  'B-JSON', now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0007',10,'D','D-Description','D-Tagline', '1240', 1111.32, 1111.12,  'D-JSON', now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0005',11,'B4New4','B4New-Description','B4-Tagline', '1238', 43.34, 43.14,  'B2-JSON', now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) 
values(289,'I0007',11,'D4','D-Description','D-Tagline', '1240', 1111.32, 1112.12,  'D-JSON', now());

*/
		
		//attemptCount++;
		
		//return null;
	}

	/* (non-Javadoc)
	 * @see com.neolynks.curator.manager.VendorAdapter#generateCompleteInventorySet()
	 */
	@Override
	public List<ItemMaster> getAllInventory() {
		List<ItemMaster> response = new ArrayList<ItemMaster>();
		response.add(itemList.get(itemList.size()-1));
		response.add(new ItemMaster(null, 1234567892L, "ICode002", null, "IName002-3", "ITagLine002-2",
				"IDesc002-3", 92.91D, 92.90D, null, null, null, new Date(System.currentTimeMillis())));
		response.add(new ItemMaster(null, 1234567893L, "ICode003", null, "IName003-3", "ITagLine003-2",
				"IDesc003-3", 93.91D, 93.90D, null, null, null, new Date(System.currentTimeMillis())));
		response.add(new ItemMaster(null, 1234567894L, "ICode004", null, "IName004-3", "ITagLine004-2",
				"IDesc004-3", 94.91D, 94.90D, null, null, null, new Date(System.currentTimeMillis())));
		response.add(new ItemMaster(null, 1234567895L, "ICode005", null, "IName005-3", "ITagLine005-2",
				"IDesc005-3", 95.91D, 95.90D, null, null, null, new Date(System.currentTimeMillis())));
		return response;
	}

}
