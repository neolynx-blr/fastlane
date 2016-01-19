package com.neolynks;

import com.neolynks.dto.CartDelta;
import com.neolynks.dto.CartOperation;
import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by nishantgupta on 19/1/16.
 */
public class CartSignalExchange {

    @Getter
    private final Queue<CartDelta> cartDeltaQueue;

    private final Queue<CartOperation> cartOperationQueue;

    @Getter
    private static CartSignalExchange instance = new CartSignalExchange();

    private CartSignalExchange(){
        this.cartDeltaQueue = new LinkedBlockingQueue<>();
        this.cartOperationQueue = new LinkedBlockingQueue<>();
    }

    public CartDelta getCartDelta(){
        return this.cartDeltaQueue.poll();
    }

    public void addCartDelta(CartDelta cartDelta){
        this.cartDeltaQueue.add(cartDelta);
    }

    public CartOperation getCartOperation(){
        return this.cartOperationQueue.poll();
    }

    public void addCartDelta(CartOperation cartOperation){
        this.cartOperationQueue.add(cartOperation);
    }

}
