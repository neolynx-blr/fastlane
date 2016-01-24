package com.neolynks.curator.exception;

/**
 * Created by nishantgupta on 24/1/16.
 */
public class CacheException extends RuntimeException {

    public CacheException(String msg){
        super(msg);
    }

    public CacheException(String msg, Throwable t){
        super(msg, t);
    }

    public CacheException(Throwable t){
        super(t);
    }
}
