package com.neolynks.worker.util;

import com.neolynks.api.common.UserVendorContext;
import com.neolynks.api.common.WorkerVendorContext;
import lombok.Getter;

/**
 * Created by nishantgupta on 15/1/16.
 */
public class WorkerContextThreadLocal {
    @Getter
    private static final ThreadLocal<WorkerVendorContext> workerVendorContextThreadLocal = new ThreadLocal<>();
}
