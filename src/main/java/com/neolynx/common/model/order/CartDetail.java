/**
 * 
 */
package com.neolynx.common.model.order;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * Created by nitesh.garg on Oct 22, 2015
 *
 */

@Data
public class CartDetail implements Serializable {

	private static final long serialVersionUID = 4571713612630143174L;
	
	private Long vendorId;
	private Long deviceDataVersionId;
	
	private UserDetail userDetail;

	//private List<ItemDetail> itemList;
	
	private List<ItemDetail> itemListForPickup;
	private List<ItemDetail> itemListForDelivery;
	
	private Double taxAmount;
	private Double discountAmount;
	private Double netAmount;
	
	//Update order request
	private String orderId;
	private Long lastKnownServerDataVersionId;
	
	/**
	 * Add some intelligence on the device side for capturing all the updated to
	 * the order once the order-id has been generated from the server side and
	 * user makes changes. At this stage, the changes could be in count or
	 * delivery marking.
	 */
	//private List<String> itemCodesAdded;
	//private List<String> itemCodesUpdated;
	
	private Boolean isPickUpItemListUpdated = Boolean.FALSE;
	private Boolean isDeliveryItemListUpdated = Boolean.FALSE;

}
