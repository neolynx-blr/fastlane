package com.neolynks.curator.cache.order;

import com.google.gson.Gson;
import com.neolynks.curator.dto.Order;
import com.neolynks.curator.exception.CacheException;
import com.neolynks.dao.IDAOEssential;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.inject.Inject;

/**
 * Created by nishantgupta on 24/1/16.
 */
@Slf4j
public class OrderCache {

    private final String ORDER_CACHE_PREFIX = "ORDER_";
    private final JedisPool jedisPool;

    private static Gson gson = new Gson();

    @Inject
    public OrderCache(JedisPool jedisPool){
        this.jedisPool = jedisPool;
    }

    public void updateCache(String orderId, Order order) throws CacheException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.set(ORDER_CACHE_PREFIX + orderId, gson.toJson(order));

        }catch (JedisConnectionException e){
            log.error("Redis connection exception", e);
            throw new CacheException(e);
        }
        finally {
            ////
        }
    }

    public Order get(String orderId) throws CacheException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String orderJson = jedis.get(ORDER_CACHE_PREFIX + orderId);
            return gson.fromJson(orderJson, Order.class);
        }catch (JedisConnectionException e){
            log.error("Redis connection exception", e);
            throw new CacheException(e);
        }
        finally {
            ////
        }
    }

}
