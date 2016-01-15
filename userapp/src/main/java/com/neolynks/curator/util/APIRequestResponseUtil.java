package com.neolynks.curator.util;


import com.neolynks.api.common.UserVendorContext;
import com.neolynks.api.common.VendorInventorySnap;

/**
 * Created by nishantgupta on 15/1/16.
 */
public class APIRequestResponseUtil {

    //user-id, vendor-id, version-id
    private static final String REQUEST_TOKEN_DELIMITER = "_";

    public static UserVendorContext getUserVendorContext(String requestToken){
        String[] splitReqToken = requestToken.split(REQUEST_TOKEN_DELIMITER);
        UserVendorContext userVendorContext = new UserVendorContext();
        userVendorContext.setUserId(splitReqToken[0]);
        userVendorContext.setVendorInventorySnap(new VendorInventorySnap(
                Long.parseLong(splitReqToken[1]),
                Integer.parseInt(splitReqToken[2])
        ));
        return userVendorContext;
    }

}
