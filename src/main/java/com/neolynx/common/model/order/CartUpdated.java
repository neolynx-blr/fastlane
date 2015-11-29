/**
 * 
 */
package com.neolynx.common.model.order;

import lombok.Data;

/**
 * Created by nitesh.garg on Nov 23, 2015
 *
 */

@Data
public class CartUpdated {

	//Update order request
	private String orderId;
	private Long lastKnownServerDataVersionId;
	
	/**
	 * Add some intelligence on the device side for capturing all the updated to
	 * the order once the order-id has been generated from the server side and
	 * user makes changes. At this stage, the changes could be in count or
	 * delivery marking.
	 */
	
	private Boolean isItemListUpdated = Boolean.FALSE;
	private Boolean isUserDetailUpdated = Boolean.FALSE;

}
