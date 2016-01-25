package com.neolynks.curator.exception;

import lombok.Getter;

/**
 * Created by nishantgupta on 25/1/16.
 */
public class InvalidStatusIdException extends Exception {

    public InvalidStatusIdException(String orderId, int statusId){
        super("Invalid statusId: " + statusId + " for orderId: " + orderId);
    }

}

