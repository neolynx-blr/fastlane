package com.neolynks.signal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by nishantgupta on 19/1/16.
 */
@Data
@AllArgsConstructor
public class CartDelta implements Serializable{

    private final String cartId;
    private final String barCode;
    private final int count;
    private Operation operation;

    public enum Operation {
        ADDED,
        REMOVED
    }
}
