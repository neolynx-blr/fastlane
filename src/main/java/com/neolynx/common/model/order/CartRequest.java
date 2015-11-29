package com.neolynx.common.model.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Created by nitesh.garg on Nov 28, 2015
 *
 */

@Data
public class CartRequest implements Serializable {

	private static final long serialVersionUID = -532841197586404513L;

	private Long vendorId;
	private Long deviceDataVersionId;

	private UserDetail userDetail;

	private DeliveryMode deliveryMode = DeliveryMode.IN_STORE_PICKUP;
	private List<ItemRequest> itemList = new ArrayList<ItemRequest>();

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
	private CartUpdated updateCart = new CartUpdated();
	
}
