package com.neolynks.api.userapp.price;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by nitesh.garg on Dec 13, 2015
 *
 */

@Data
public class DiscountBase implements Serializable {

	private static final long serialVersionUID = -556123350699505821L;

	private Double value = 1.0D;
	private DiscountUnit unit = DiscountUnit.COUNT;
	private DiscountPlayer player = DiscountPlayer.SELF;
}
