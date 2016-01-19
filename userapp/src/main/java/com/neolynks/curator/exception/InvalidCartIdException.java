package com.neolynks.curator.exception;

import lombok.Getter;

/**
 * Created by nishantgupta on 19/1/16.
 */
public class InvalidCartIdException extends Exception {

    @Getter
    public static InvalidCartIdException defaut = new InvalidCartIdException("Invalid or Missing CartId in request");

    public InvalidCartIdException(String msg){
        super(msg);
    }


}
