package com.neolynks.api.userapp.price;

import lombok.Data;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by nitesh.garg on Oct 26, 2015
 *
 */

@Data
@Builder
public class ItemPrice implements Serializable {

	private static final long serialVersionUID = 8222498504856208090L;

	private Double mrp;
	private Double sellingPrice;
	private Double basePrice;
	private TaxDetail taxDetail;
	private DiscountDetail discountDetail;

}
