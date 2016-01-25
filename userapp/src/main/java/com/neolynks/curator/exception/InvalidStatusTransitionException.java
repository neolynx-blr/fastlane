package com.neolynks.curator.exception;

import com.neolynks.api.common.OrderStatus;

/**
 * Created by nishantgupta on 25/1/16.
 */
public class InvalidStatusTransitionException extends Exception {

    public InvalidStatusTransitionException(String orderId, OrderStatus oldStatus, OrderStatus newStatus){
        super("Invalid order transition from oldStatus: " + oldStatus + " to newStatus: " + newStatus
            + " for orderId: " + orderId);
    }
}
