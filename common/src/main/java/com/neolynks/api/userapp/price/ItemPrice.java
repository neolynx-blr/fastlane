package com.neolynks.api.userapp.price;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by nitesh.garg on Oct 26, 2015
 *
 */

@Data
public class ItemPrice implements Serializable {

	private static final long serialVersionUID = 8222498504856208090L;

	private Double mrp;
	private Double sellingPrice;
	private Double basePrice;
	private TaxDetail taxDetail;
	private DiscountDetail discountDetail;

	public ItemPrice generateDifferentialFrom(ItemPrice newItemPrice) {

		if (newItemPrice != null) {

			ItemPrice differentialItemPrice = new ItemPrice();

			if (this.getMrp() != newItemPrice.getMrp()){
				differentialItemPrice.setMrp(newItemPrice.getMrp());
			}

			if (this.getSellingPrice() != newItemPrice.getSellingPrice()) {
				differentialItemPrice.setSellingPrice(newItemPrice.getSellingPrice());
			}

			if (this.getBasePrice() != newItemPrice.getBasePrice()) {
				differentialItemPrice.setBasePrice(newItemPrice.getBasePrice());
			}

			if (this.getTaxDetail() == null || !this.getTaxDetail().equals(newItemPrice.getTaxDetail())) {
				differentialItemPrice.setTaxDetail(newItemPrice.getTaxDetail());
			}

			if (this.getDiscountDetail() == null
					|| !this.getDiscountDetail().equals(newItemPrice.getDiscountDetail())) {
				differentialItemPrice.setDiscountDetail(newItemPrice.getDiscountDetail());
			}
			return differentialItemPrice;
		}

		return null;
	}


}
