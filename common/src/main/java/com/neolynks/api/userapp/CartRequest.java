package com.neolynks.api.userapp;

import lombok.Data;

import java.util.Map;

/**
 * Created by nishantgupta on 17/1/16.
 */
@Data
public class CartRequest {
    private boolean completeSnap;
    private Map<String, Integer> itemCount;
}
