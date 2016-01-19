package com.neolynks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by nishantgupta on 19/1/16.
 */
@Data
@AllArgsConstructor
public class CartOperation {

    private final String cartId;

    private final long vendorId;
    //move this to enum
    private final int cartStatus;

}
