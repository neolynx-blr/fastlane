package com.neolynks.app.notification;

import com.neolynks.api.common.OrderStatus;
import lombok.Getter;

/**
 * Created by nishantgupta on 24/1/16.
 * Source: Read - https://github.com/writtmeyer/gcm_server might help
 */
public class UserAppNotificationSignal {

    @Getter
    private static UserAppNotificationSignal userAppNotificationSignal = new UserAppNotificationSignal();

    public void publishUserAppNotification(String userId, String cartId, OrderStatus orderStatus){
        //
    }


}
