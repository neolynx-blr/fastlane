package com.neolynks.signal.dto;

import com.neolynks.api.common.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by nishantgupta on 19/1/16.
 */
@Data
@AllArgsConstructor
public class CartOperation implements Serializable{

    private final String cartId;

    private final long vendorId;

    private final OrderStatus orderStatus;

}
