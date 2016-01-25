package com.neolynks.api.common;

import lombok.Data;

/**
 * Created by nishantgupta on 24/1/16.
 */
@Data
public class WorkerVendorContext {
    private String workerSessionId;
    private VendorInventorySnap vendorInventorySnap;
}
