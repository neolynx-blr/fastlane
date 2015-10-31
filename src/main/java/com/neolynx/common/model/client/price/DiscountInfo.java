package com.neolynx.common.model.client.price;

import lombok.Data;

/**
 * Created by nitesh.garg on Oct 28, 2015
 *
 */

@Data
public class DiscountInfo {

	private Integer discountType;
	private Double discountValue;
	private Integer requiredCountForDiscount;
	private String discountedItemCode;

	public Boolean isDifferent(DiscountInfo discountInfo) {

		if (discountInfo == null) {
			return Boolean.TRUE;
		}

		if ((this.discountType == null && discountInfo.getDiscountType() != null)
				|| (this.discountType.compareTo(discountInfo.getDiscountType()) != 0)) {
			return Boolean.TRUE;
		}

		if ((this.discountValue == null && discountInfo.getDiscountValue() != null)
				|| (this.discountValue.compareTo(discountInfo.getDiscountValue()) != 0)) {
			return Boolean.TRUE;
		}

		if ((this.discountedItemCode == null && discountInfo.getDiscountedItemCode() != null)
				|| (this.discountedItemCode.compareTo(discountInfo.getDiscountedItemCode()) != 0)) {
			return Boolean.TRUE;
		}

		if ((this.requiredCountForDiscount == null && discountInfo.getRequiredCountForDiscount() != null)
				|| (this.requiredCountForDiscount.compareTo(discountInfo.getRequiredCountForDiscount()) != 0)) {
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

}
