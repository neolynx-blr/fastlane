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

	private List<ItemDetail> itemList;

	/**
	 * NOTE that if delivery mode is changed, ensure that user-detail update
	 * flag is set to true because this will impact the delivery address being
	 * used or not form the user detail object
	 */
	private DeliveryMode deliveryMode = DeliveryMode.IN_STORE_PICKUP;

	private Double taxAmount;
	private Double taxableAmount;
	private Double discountAmount;

	private Double netAmount;

	private int itemCount;
	private int totalCount;

	/**
	 * This object is used to indicate that order is updated now, although DO
	 * NOTE that fields present above may also be modified in the update
	 * request. Will be ignored for create order calls.
	 */
	private UpdateCartDetail updateCart;
}
