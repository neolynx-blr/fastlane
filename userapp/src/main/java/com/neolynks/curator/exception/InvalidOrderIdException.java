package com.neolynks.curator.exception;

import lombok.Getter;

/**
 * Created by nishantgupta on 19/1/16.
 */
public class InvalidOrderIdException extends Exception {

    @Getter
    public static InvalidOrderIdException defaut = new InvalidOrderIdException("Invalid or Missing CartId in request");

    public InvalidOrderIdException(String msg){
        super(msg);
    }


}
