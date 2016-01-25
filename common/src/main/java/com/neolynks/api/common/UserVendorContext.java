package com.neolynks.api.common;

import lombok.Data;

/**
 * Created by nishantgupta on 15/1/16.
 */
@Data
public class UserVendorContext {
    private String userId;
    private VendorInventorySnap vendorInventorySnap;
}
