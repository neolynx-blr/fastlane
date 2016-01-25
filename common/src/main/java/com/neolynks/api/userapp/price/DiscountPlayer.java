/**
 * 
 */
package com.neolynks.api.userapp.price;

/**
 * Created by nitesh.garg on Dec 13, 2015
 *
 */
public enum DiscountPlayer {

	SELF(1), TRANSACTION(2), BRAND(3), CATEGORY(4), ANOTHER_ITEM(5);
	
	private final int value;

	private DiscountPlayer(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}


}
