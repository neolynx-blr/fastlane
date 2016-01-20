package com.neolynks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by nishantgupta on 19/1/16.
 */
@Data
@AllArgsConstructor
public class CartDelta {
    private final String cartId;
    private final String barCode;
    private final int count;
    private Operation operation;

    public enum Operation {
        ADDED,
        REMOVED
    }
}
