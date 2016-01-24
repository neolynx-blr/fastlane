package com.neolynks.worker.filter;

import com.neolynks.worker.annotation.WorkerContextRequired;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * Created by nishantgupta on 24/1/16.
 */
@Provider
public class WorkerContextFeature implements DynamicFeature {
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (resourceInfo.getResourceMethod().getAnnotation(WorkerContextRequired.class) != null) {
            context.register(WorkerVendorContextFilter.class);
        }
    }
}