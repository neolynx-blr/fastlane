package com.neolynks.api.common;

import lombok.Data;

/**
 * Created by nishantgupta on 15/1/16.
 */
@Data
public class VendorInventorySnap {
    private final long vendorId;
    private final int deviceDataVersionId;
}
