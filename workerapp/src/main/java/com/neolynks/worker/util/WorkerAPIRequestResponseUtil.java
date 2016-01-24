package com.neolynks.worker.util;


import com.neolynks.api.common.UserVendorContext;
import com.neolynks.api.common.VendorInventorySnap;
import com.neolynks.api.common.WorkerVendorContext;

/**
 * Created by nishantgupta on 15/1/16.
 */
public class WorkerAPIRequestResponseUtil {

    //workerSession-id, vendor-id, version-id
    private static final String REQUEST_TOKEN_DELIMITER = "_";

    public static WorkerVendorContext getWorkerVendorContext(String requestToken){
        String[] splitReqToken = requestToken.split(REQUEST_TOKEN_DELIMITER);
        WorkerVendorContext workerVendorContext = new WorkerVendorContext();
        workerVendorContext.setWorkerSessionId(splitReqToken[0]);
        workerVendorContext.setVendorInventorySnap(new VendorInventorySnap(
                Long.parseLong(splitReqToken[1]),
                Integer.parseInt(splitReqToken[2])
        ));
        return workerVendorContext;
    }

}
