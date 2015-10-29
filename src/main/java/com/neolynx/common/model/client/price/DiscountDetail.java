/**
 * 
 */
package com.neolynx.common.model.client.price;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Created by nitesh.garg on Oct 28, 2015
 *
 */

@Data
public class DiscountDetail{
	
	private List<DiscountInfo> discountInfo = new ArrayList<DiscountInfo>();

}
