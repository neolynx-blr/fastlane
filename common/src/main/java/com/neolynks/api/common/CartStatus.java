package com.neolynks.api.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by nitesh.garg on Dec 27, 2015
 *
 */
@AllArgsConstructor
public enum CartStatus {

	OPEN(1), IN_PREPARATION(2), PENDING_USER_REVIEW(3), PENDING_PAYMENT(4), PENDING_DELIVERY(5), PENDING_PARTIAL_DELIVERY(6), COMPLETE(7);

    @Getter
	private final int value;

}
