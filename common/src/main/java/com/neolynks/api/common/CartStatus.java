package com.neolynks.api.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nitesh.garg on Dec 27, 2015
 *
 */
@AllArgsConstructor
public enum CartStatus {

	OPEN(1),
    IN_PREPARATION(2),
    PENDING_USER_REVIEW(3),
    PENDING_PAYMENT(4),
    PENDING_DELIVERY(5),
    PENDING_PARTIAL_DELIVERY(6),
    COMPLETE(7),
    DISCARDED(8),
    CLOSED(9);

    @Getter
	private final int value;

    private static Map<Integer, CartStatus> reverseMap = new HashMap<>();

    static{
        for(CartStatus cartStatus: CartStatus.values()){
            reverseMap.put(cartStatus.getValue(), cartStatus);
        }
    }

    public static CartStatus getStatus(int value){
        return reverseMap.get(value);
    }

}
