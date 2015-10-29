/**
 * 
 */
package com.neolynx.common.model.client.price;

import lombok.Data;

/**
 * Created by nitesh.garg on Oct 26, 2015
 *
 */

@Data
public class ItemPrice implements Comparable<ItemPrice> {

	private Double mrp;
	private Double price;
	private Double basePrice;

	private TaxDetail taxDetail;
	private DiscountDetail discountDetail;
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ItemPrice that) {

		if (this.getMrp().compareTo(that.getMrp()) == 0 && this.getPrice().compareTo(that.getPrice()) == 0
				&& this.getBasePrice().compareTo(that.getBasePrice()) == 0) {
			return 0;
		}
		
		return 1;
	}
	
}
