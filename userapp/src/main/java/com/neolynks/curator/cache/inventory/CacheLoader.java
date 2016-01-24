package com.neolynks.curator.cache.inventory;

/**
 * Created by nishantgupta on 17/1/16.
 */
public interface CacheLoader<K, V> {
    public V load(K vendorVersionKey) throws Exception;
}
