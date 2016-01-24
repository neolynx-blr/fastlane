package com.neolynks.api.userapp.price;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Builder;

/**
 * Created by nishantgupta on 24/1/16.
 */
@Data
@Builder
public class OrderPrice {
    private Double netAmount;
    private Double taxAmount;
    private Double taxableAmount;
    private Double discountAmount;
}
