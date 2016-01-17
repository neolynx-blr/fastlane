package com.neolynks.curator.cache;

import com.neolynks.api.common.Response;
import com.neolynks.api.common.inventory.InventoryInfo;

/**
 * Created by nishantgupta on 17/1/16.
 */
public interface CacheLoader<K, V> {
    public V load(K vendorVersionKey) throws Exception;
}
