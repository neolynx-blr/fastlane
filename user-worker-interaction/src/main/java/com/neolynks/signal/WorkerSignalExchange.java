package com.neolynks.signal;

import com.neolynks.signal.dto.CartOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by nishantgupta on 19/1/16.
 */
@Slf4j
public class WorkerSignalExchange {

    public static final String ORDER_UPDATES = "order_updates_";

    private final JedisPool jedisPool;
    private final CustomerSubscriber subscriber;

    private Jedis subscriberJedis;


    public WorkerSignalExchange(JedisPool jedisPool, ISignalProcessor iSignalProcessor){
        this.jedisPool  = jedisPool;
        this.subscriber = new CustomerSubscriber(iSignalProcessor);
    }

     public void init(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Jedis subscriberJedis = jedisPool.getResource();
                    subscriberJedis.subscribe(subscriber, ORDER_UPDATES.getBytes());
                } catch (Exception e) {
                    log.error("Some error in worker subscriber thread:", e);
                }
            }
        }, "subscriberThread").start();
    }

    public void shutDown(){
        if(subscriber != null){
            subscriber.unsubscribe();
        }
        if(subscriberJedis != null){
            subscriberJedis.quit();
        }
    }

    public void publishCartStatus(CartOperation cartOperation){
        Jedis jedisPublisher = jedisPool.getResource();
        jedisPublisher.publish(ORDER_UPDATES.getBytes(), SerializationUtils.serialize(cartOperation));
        jedisPool.returnResource(jedisPublisher);
    }


    @Slf4j
    @AllArgsConstructor
    public static class CustomerSubscriber extends BinaryJedisPubSub {

        private final ISignalProcessor iSignalProcessor;

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            log.info("Message received. Channel: {}, Msg: {}", channel, message);
            iSignalProcessor.process(message);
        }

        @Override
        public void onPMessage(byte[] pattern, byte[] channel, byte[] message) {

        }

        @Override
        public void onSubscribe(byte[] channel, int subscribedChannels) {

        }

        @Override
        public void onUnsubscribe(byte[] channel, int subscribedChannels) {

        }

        @Override
        public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {

        }

        @Override
        public void onPSubscribe(byte[] pattern, int subscribedChannels) {

        }
    }

}
