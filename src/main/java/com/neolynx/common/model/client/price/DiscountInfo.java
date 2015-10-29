/**
 * 
 */
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

}
