package com.neolynks;

import com.neolynks.dto.CartDelta;
import com.neolynks.dto.CartOperation;
import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by nishantgupta on 19/1/16.
 */
public class CartSignalExchange {

    private final Queue<CartDelta> cartDeltaQueue;
    private final Queue<CartOperation> cartOperationQueue;

    @Getter
    private static CartSignalExchange instance = new CartSignalExchange();

    private CartSignalExchange(){
        this.cartDeltaQueue = new ConcurrentLinkedQueue<>();
        this.cartOperationQueue = new ConcurrentLinkedQueue<>();
    }

    public CartDelta getCartDelta(){
        return this.cartDeltaQueue.peek();
    }

    public void addCartDelta(CartDelta cartDelta){
        this.cartDeltaQueue.add(cartDelta);
    }

    public CartOperation getCartOperation(){
        return this.cartOperationQueue.peek();
    }

    public void removeCartOperation(){
        this.cartOperationQueue.poll();
    }

    public void removeCartDelta(){
        this.cartOperationQueue.poll();
    }

    public void addCartDelta(CartOperation cartOperation){
        this.cartOperationQueue.add(cartOperation);
    }

}
