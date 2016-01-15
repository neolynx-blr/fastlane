package com.neolynks.curator.util;

import com.neolynks.api.common.UserVendorContext;
import lombok.Getter;

/**
 * Created by nishantgupta on 15/1/16.
 */
public class UserContextThreadLocal {
    @Getter
    private static final ThreadLocal<UserVendorContext> userVendorContextLocale = new ThreadLocal<>();
}
