package com.neolynks;

import com.neolynks.curator.FastlaneConfiguration;
import com.neolynks.curator.RedisConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.dropwizard.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
/**
 * Created by nishantgupta on 22/1/16.
 */
public class ApplicationModule extends AbstractModule
{
    @Override
    protected void configure()
    {

    }

    @Provides
    public FastlaneConfiguration configuration(Configuration configuration)
    {
        return (FastlaneConfiguration) configuration;
    }

    @Provides
    public JedisPool provideJedisPool(FastlaneConfiguration applicationConfiguration)
    {
        RedisConfiguration redisConfig = applicationConfiguration.getRedis();
        return new JedisPool(
                new JedisPoolConfig(),
                redisConfig.getHostname(),
                redisConfig.getPort() );
    }
}