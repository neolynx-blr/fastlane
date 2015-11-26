/**
 * 
 */
package com.neolynx.common.model.order;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nitesh.garg on Oct 25, 2015
 *
 */
public class CartCalculator {
	
	public static CartDetail calculatePrice(CartDetail cart) {
		
		Double totalMRP = 0.0D;
		Double netAmount = 0.0D;
		
		Double taxAmount = 0.0D;
		Double taxableAmount = 0.0D;
		Double discountAmount = 0.0D;
		
		List<ItemDetail> allItemList = new ArrayList<ItemDetail>();
		
		allItemList.addAll(cart.getItemList());
		
		for(ItemDetail instance : allItemList) {
			netAmount += instance.getNetPrice();
			taxAmount += instance.getNetTaxAmount();
			taxableAmount += instance.getNetTaxableAmount();
			discountAmount += instance.getNetDiscountAmount();

			totalMRP += (instance.getItemPrice().getMrp() * instance.getCount());
		}
		
		cart.setNetAmount(netAmount);
		cart.setTaxAmount(taxAmount);
		cart.setTaxableAmount(taxableAmount);
		cart.setDiscountAmount(discountAmount);
		
		return cart;
		
	}

}
