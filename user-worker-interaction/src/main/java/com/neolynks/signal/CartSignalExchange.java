package com.neolynks.signal;

import com.neolynks.signal.dto.CartDelta;
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
@AllArgsConstructor
@Slf4j
public class CartSignalExchange {

    public static final String ORDER_DELTA_CHANNEL_NAME = "order_delta_";
    private static final String ORDER_OPERATION_CHANNEL_NAME = "order_operation_";

    private final JedisPool jedisPool;
    private final WorkerSubscriber orderSubscriber;

    private Jedis subscriberJedis;

    public CartSignalExchange(JedisPool jedisPool, ISignalProcessor orderDeltaProcessor,
                              ISignalProcessor orderOperationProcessor){
        this.jedisPool  = jedisPool;
        this.orderSubscriber = new WorkerSubscriber(orderDeltaProcessor, orderOperationProcessor);
    }


    public void init(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Jedis subscriberJedis = jedisPool.getResource();
                    subscriberJedis.subscribe(orderSubscriber, ORDER_DELTA_CHANNEL_NAME.getBytes(), ORDER_OPERATION_CHANNEL_NAME.getBytes());
                } catch (Exception e) {
                    log.error("Some error in worker orderSubscriber thread:", e);
                }
            }
        }, "subscriberThread").start();
    }

    public void shutDown(){
        if(orderSubscriber != null){
            orderSubscriber.unsubscribe();
        }
        if(subscriberJedis != null){
            subscriberJedis.quit();
        }
    }

    public void publishCartDelta(CartDelta cartDelta){
        Jedis jedisPublisher = jedisPool.getResource();
        jedisPublisher.publish(ORDER_DELTA_CHANNEL_NAME.getBytes(), SerializationUtils.serialize(cartDelta));
        jedisPool.returnResource(jedisPublisher);
    }

    public void publishCartOperation(CartOperation cartOperation){
        Jedis jedisPublisher = jedisPool.getResource();
        jedisPublisher.publish(ORDER_OPERATION_CHANNEL_NAME.getBytes(), SerializationUtils.serialize(cartOperation));
        jedisPool.returnResource(jedisPublisher);
    }

    @Slf4j
    @AllArgsConstructor
    public static class WorkerSubscriber extends BinaryJedisPubSub {

        private final ISignalProcessor orderDeltaProcessor;
        private final ISignalProcessor orderOperationProcessor;

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            String channelName = (String)SerializationUtils.deserialize(channel);
            if(ORDER_DELTA_CHANNEL_NAME.equals(channelName)){
                log.info("Message received. Channel: {}, Msg: {}", channel, message);
                orderDeltaProcessor.process(message);
            }else if(ORDER_OPERATION_CHANNEL_NAME.equals(channelName)){
                log.info("Message received. Channel: {}, Msg: {}", channel, message);
                orderOperationProcessor.process(message);
            }
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
