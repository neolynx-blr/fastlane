package com.neolynks.curator.filter;

import com.neolynks.curator.annotation.UserContextRequired;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * Created by nishantgupta on 15/1/16.
 */
@Provider
public class CustomerContextFeature implements DynamicFeature {
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (resourceInfo.getResourceMethod().getAnnotation(UserContextRequired.class) != null) {
            context.register(CustomerVendorContextFilter.class);
        }
    }
}