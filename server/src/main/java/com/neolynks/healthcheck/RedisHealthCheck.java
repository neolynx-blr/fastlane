package com.neolynks.healthcheck;


import com.google.inject.Inject;
import com.yammer.metrics.core.HealthCheck;
import redis.clients.jedis.JedisPool;
/**
 * Created by nishantgupta on 24/1/16.
 */
public class RedisHealthCheck extends HealthCheck
{
    private static final String ECHO = "checking";
    private final JedisPool jedisPool;

    @Inject
    public RedisHealthCheck( JedisPool jedisPool )
    {
        super("redis");
        this.jedisPool = jedisPool;
    }

    @Override
    protected Result check() throws Exception
    {
        if ( ECHO.equalsIgnoreCase(jedisPool.getResource().echo(ECHO)) )
        {
            return HealthCheck.Result.healthy();
        }
        else
        {
            return HealthCheck.Result.unhealthy("Something was wrong");
        }
    }
}