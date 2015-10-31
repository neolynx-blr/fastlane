/**
 * 
 */
package com.neolynx.curator.core;

import com.neolynx.common.model.client.ItemInfo;

/**
 * Created by nitesh.garg on Oct 30, 2015
 *
 */
public class VendorItemMasterWrapper {

	public ItemInfo generateDifferential(VendorItemMaster oldItem, VendorItemMaster newItem) {
		
		ItemInfo deltaItemInfo = new ItemInfo(oldItem);
		deltaItemInfo.updateThisWithLatestItemData(newItem);
		
		return deltaItemInfo;
		
	}
	
}
