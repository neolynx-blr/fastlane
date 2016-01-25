package com.neolynks.worker.filter;

import com.neolynks.api.common.WorkerVendorContext;
import com.neolynks.worker.util.WorkerAPIRequestResponseUtil;
import com.neolynks.worker.util.WorkerContextThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Created by nishantgupta on 23/1/16.
 */
@Provider
public class WorkerVendorContextFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String REQUEST_TOKEN = "requestToken";
    private static final String WORKER_ID_KEY = "worker-id-key";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final String requestToken = requestContext.getHeaderString(REQUEST_TOKEN);
        if (!StringUtils.isBlank(requestToken)) {
            WorkerVendorContext workerVendorContext=  WorkerAPIRequestResponseUtil.getWorkerVendorContext(requestToken);
            WorkerContextThreadLocal.getWorkerVendorContextThreadLocal().set(workerVendorContext);
            MDC.put(WORKER_ID_KEY, requestToken);
        }
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        MDC.remove(WORKER_ID_KEY);
    }
}
