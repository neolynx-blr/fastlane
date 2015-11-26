package com.neolynx.common.model.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import com.neolynx.common.model.ErrorCode;
import com.neolynx.common.model.client.ItemInfo;
import com.neolynx.common.model.client.price.ItemPrice;

/**
 * Created by nitesh.garg on Oct 22, 2015
 *
 */

@Data
public class ItemDetail implements Serializable {

	private static final long serialVersionUID = -3644032470559236894L;

	private String barcode;
	private String itemCode;
	private Integer count = 1;

	/**
	 * MRP is arbitrary number with no real implications
	 * 
	 * fn(basePrice, tax%) = taxAmount
	 * fn(basePrice, discount%) = discountAmount
	 * 
	 * price = basePrice + taxAmount - discountAmount
	 * netPrice = fn ( (count * price), cummulative-discount-if-applicable)
	 * 
	 */
	
	private Double netPrice;
	private Double netTaxAmount;
	private Double netTaxableAmount;
	private Double netDiscountAmount;

	private ItemPrice itemPrice;
	
	/**
	 * In case this item is discounted because of buying some other item (in bulk
	 * etc.), that item-code is captured here.
	 */
	private String externallyDiscountedByItem;
	
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
	
	public List<ErrorCode> selfValidate() {
		
		List<ErrorCode> response = new ArrayList<ErrorCode>();
		
		if(this.getItemCode() == null) {
			response.add(ErrorCode.INVALID_OR_MISSING_ITEM_CODE);
		}
		
		if(this.getBarcode() == null) {
			response.add(ErrorCode.INVALID_OR_MISSING_BARCODE);
		}
		
		return response;
	}
	
	public ItemDetail(ItemInfo itemInfo) {
		this.setBarcode(itemInfo.getBarcode());
		this.setItemCode(itemInfo.getItemCode());
		this.setItemPrice(itemInfo.getItemPrice());
	}
	
}
