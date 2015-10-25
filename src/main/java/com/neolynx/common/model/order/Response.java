/**
 * 
 */
package com.neolynx.common.model.order;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.neolynx.common.model.BaseResponse;
import com.neolynx.common.model.InventoryResponse;

/**
 * Created by nitesh.garg on Oct 22, 2015
 *
 */

@Data
@EqualsAndHashCode(callSuper=false)
public class Response extends BaseResponse {

	private static final long serialVersionUID = 8768826874201574453L;

	/**
	 * If things are okay, generate the order details and send back in response.
	 */
	private String orderId;
	private String orderStatus;
	private String orderBarcode;

	private Long serverDataVersionId;
	private Long deviceDataVersionId;

	/**
	 * In case the data version has changed since the last update to user device, 
	 * Option a) send the inventory update for vendor-version combination and let client handle everything
	 * Option b) send the item-details back with updated data so that this transaction can be completed quickly 
	 */
	
	// Option A
	private InventoryResponse inventoryResponse;
	
	/**
	 * Option B
	 * 
	 * Note that only items where pricing has changed will be added to the
	 * following list or else the lists will be empty
	 */
	
	private Double taxAmount;
	private Double discountAmount;
	private Double netAmount;

	private List<ItemDetail> onlyUpdatedItemListForPickup = new ArrayList<ItemDetail>();
	private List<ItemDetail> onlyUpdatedItemListForDelivery = new ArrayList<ItemDetail>();

}
