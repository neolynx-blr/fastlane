package com.neolynks;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Created by nishantgupta on 10/1/16.
 */
@NoArgsConstructor
public class UserWorkerSignalExchange {

    private static UserWorkerSignalExchange instance = new UserWorkerSignalExchange();

    @Setter
    @Getter
    private IWorkerCallbacks iWorkerCallbacks;

    public static UserWorkerSignalExchange getInstance(){
        return instance;
    }

    public void closeCart(long cartId){
        iWorkerCallbacks.closeCart(cartId);
    }

    public void addCartDelta(long cartId, Map<Long, Integer> itemsToBeSynced){
        iWorkerCallbacks.addCartDelta(cartId, itemsToBeSynced);
    }

    public void initWorkerCart(long cartId, long vendorId){
        iWorkerCallbacks.initWorkerCart(cartId, vendorId);
    }

}
