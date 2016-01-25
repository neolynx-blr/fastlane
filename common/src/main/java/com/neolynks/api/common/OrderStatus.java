package com.neolynks.api.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nitesh.garg on Dec 27, 2015
 *
 */
@AllArgsConstructor
public enum OrderStatus {

    OPEN(1, EnumSet.of(OrderStatus.ORDER_PLACED, OrderStatus.DISCARDED)),
    ORDER_PLACED(2, EnumSet.of(OrderStatus.IN_PREPARATION)),
    IN_PREPARATION(3, EnumSet.of(OrderStatus.PENDING_USER_REVIEW)),
    PENDING_USER_REVIEW(4, EnumSet.of(OrderStatus.USER_REVIEW_DONE, OrderStatus.DISCARDED)),
    USER_REVIEW_DONE(5, EnumSet.of(OrderStatus.PENDING_PAYMENT)),
    PENDING_PAYMENT(6, EnumSet.of(OrderStatus.COMPLETE)),
    COMPLETE(7, EnumSet.noneOf(OrderStatus.class)),
    DISCARDED(8, EnumSet.noneOf(OrderStatus.class));

    @Getter
	private final int value;
    @Getter
    private final EnumSet<OrderStatus> possibleNext;

    private static Map<Integer, OrderStatus> reverseMap = new HashMap<>();

    static{
        for(OrderStatus orderStatus : OrderStatus.values()){
            reverseMap.put(orderStatus.getValue(), orderStatus);
        }
    }

    public static OrderStatus getStatus(int value){
        return reverseMap.get(value);
    }

}
