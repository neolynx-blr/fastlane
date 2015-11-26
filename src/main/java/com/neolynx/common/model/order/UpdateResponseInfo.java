/**
 * 
 */
package com.neolynx.common.model.order;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import com.neolynx.common.model.client.InventoryInfo;

/**
 * Created by nitesh.garg on Nov 22, 2015
 *
 */

@Data
public class UpdateResponseInfo {

	/**
	 * In case the data version has changed since the last update to user device, 
	 * Option a) send the inventory update for vendor-version combination and let client handle everything
	 * Option b) send the item-details back with updated data so that this transaction can be completed quickly 
	 */
	
	// Option A
	private InventoryInfo inventoryResponse;
	
	/**
	 * Option B
	 * 
	 * Note that only items where pricing has changed will be added to the
	 * following list or else the lists will be empty
	 */
	
	private Double taxAmount;
	private Double taxableAmount;
	private Double discountAmount;

	private Double netAmount;

	private List<ItemDetail> onlyUpdatedItemList = new ArrayList<ItemDetail>();
	
}
