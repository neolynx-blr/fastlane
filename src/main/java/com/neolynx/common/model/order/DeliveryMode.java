/**
 * 
 */
package com.neolynx.common.model.order;

/**
 * Created by nitesh.garg on Nov 23, 2015
 *
 */

public enum DeliveryMode {

	IN_STORE_PICKUP(1), PARTIAL_DELIVERY(2), DELIVERY(3);

	private final int value;

	private DeliveryMode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
