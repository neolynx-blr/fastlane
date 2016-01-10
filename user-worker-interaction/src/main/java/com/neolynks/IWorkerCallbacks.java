package com.neolynks;

import java.util.Map;

/**
 * Created by nishantgupta on 10/1/16.
 */
public interface IWorkerCallbacks {

    public void closeCart(long cartId);

    public void addCartDelta(long cartId, Map<Long, Integer> itemsToBeSyced);

    public void initWorkerCart(long cartId, long vendorId);

}
