package com.neolynks.curator.filter;

import com.neolynks.api.common.UserVendorContext;
import com.neolynks.curator.util.APIRequestResponseUtil;
import com.neolynks.curator.util.UserContextThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Created by nishantgupta on 15/1/16.
 */
@Provider
public class CustomerVendorContextFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String REQUEST_TOKEN = "requestToken";
    private static final String USER_ID_KEY = "user-id-key";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final String requestToken = requestContext.getHeaderString(REQUEST_TOKEN);
        if (!StringUtils.isBlank(requestToken)) {
            UserVendorContext userVendorContext=  APIRequestResponseUtil.getUserVendorContext(requestToken);
            UserContextThreadLocal.getUserVendorContextLocale().set(userVendorContext);
            MDC.put(USER_ID_KEY, userVendorContext.getUserId());
        }
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        MDC.remove(USER_ID_KEY);
    }
}
