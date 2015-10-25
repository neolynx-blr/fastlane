/**
 * 
 */
package com.neolynx.common.model.order;

import java.io.Serializable;

import com.neolynx.curator.core.DiscountType;

import lombok.Data;

/**
 * Created by nitesh.garg on Oct 22, 2015
 *
 */

@Data
public class ItemDetail implements Serializable {

	private static final long serialVersionUID = -3644032470559236894L;

	private String itemCode;
	private Integer count;
	
	private Double price;
	private Double taxAmount;
	private Double netItemPrice;
	
	private DiscountType discountType;
	private Double netPrice;
	
	/**
	 * Indicates if the price based on current vendor data version is different
	 * from what is sent form user-device side. In such cases, send the details
	 * back so that device can close out transaction quickly rather than another
	 * server call. So, this flag is used when responding back to device for a
	 * order create/update request.
	 */
	private Boolean isPricingChanged;
	
	/**
	 * In case this particular item is marked for delivery by the user. The
	 * address needs to be available at the request level. Besides, in case
	 * count of an item is split for delivery and in-store pick-up, from the
	 * client side create different item-details in the request.
	 */
	private Boolean isMarkedForDelivery;
}
