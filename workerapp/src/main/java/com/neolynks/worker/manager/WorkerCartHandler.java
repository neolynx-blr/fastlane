package com.neolynks.worker.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.neolynks.signal.CartSignalExchange;
import com.neolynks.signal.ISignalProcessor;
import com.neolynks.api.common.CartStatus;
import com.neolynks.signal.WorkerSignalExchange;
import com.neolynks.signal.dto.CartDelta;
import com.neolynks.signal.dto.CartOperation;
import com.neolynks.worker.exception.WorkerException;
import com.neolynks.worker.exception.WorkerException.WORKER_CART_ERROR;
import com.neolynks.worker.dto.WorkerCart;
import com.neolynks.worker.dto.WorkerSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import redis.clients.jedis.Jedis;

/**
 * 
 * @author abhishekshukla
 * This class is thread safe implementation of 
 * Worker side cart corresponding to user side cart
 * Class is expected to have high concurrency as traffic increases.
 * Class expect for every new order there will be a new cart id.
 */
@Slf4j
@AllArgsConstructor
public class WorkerCartHandler{

	// add WorkerCartDao here 
	private final ConcurrentHashMap<String, WorkerCart> idToWorkerCartMap = new ConcurrentHashMap<>();
	private final WorkerSessionHandler workerSessionHandler;
    private final CartSignalExchange cartSignalExchange;
    private final WorkerSignalExchange workerSignalExchange;

    /***************************************/

    public class OderDeltaProcessor implements ISignalProcessor{

        @Override
        public void process(byte[] message) {
            CartDelta cartDelta = (CartDelta)SerializationUtils.deserialize(message);
            if (cartDelta != null) {
                if(cartDelta.getOperation().equals(CartDelta.Operation.ADDED)) {
                    Map<String, Integer> newItemCountMap = new HashMap<>();
                    newItemCountMap.put(cartDelta.getBarCode(), cartDelta.getCount());
                    addCartDelta(cartDelta.getCartId(), newItemCountMap);
                }
            }
        }
    }

    public class OderOperationProcessor implements ISignalProcessor{
        @Override
        public void process(byte[] message) {
            CartOperation cartOperation = (CartOperation)SerializationUtils.deserialize(message);
            if (cartOperation != null) {
                if (cartOperation.getCartStatus().equals(CartStatus.OPEN)) {
                    initWorkerCart(cartOperation.getCartId(), cartOperation.getVendorId());
                } else if (cartOperation.getCartStatus().equals(CartStatus.COMPLETE)){
                    closeCart(cartOperation.getCartId());
                }else if(cartOperation.getCartStatus().equals(CartStatus.DISCARDED)){
                    discardCart(cartOperation.getCartId());
                }
            }
        }
    }

    /***************************************/
    
   	private WorkerCart getWorkerCart(String cartId) {
		WorkerCart workerCart = idToWorkerCartMap.get(cartId);
		if (null == workerCart) {
			throw new WorkerException(WORKER_CART_ERROR.UNKNOWN_CART_ID);
		}
		return workerCart;
	}

	private void initWorkerCart(String cartId, long storeId) {
		WorkerCart workerCart = new WorkerCart(cartId, storeId);
		if (null != idToWorkerCartMap.putIfAbsent(cartId, workerCart) ) {
			workerSessionHandler.addWorkerCartForWorkerSessionAssignment(workerCart);
		} else {
			throw new WorkerException(WORKER_CART_ERROR.DUPLICATE_CART_ID);
		}
	}

    private void addCartDelta(String cartId, Map<String, Integer> newItemCountMap) {
		WorkerCart workerCart = getWorkerCart(cartId);
		workerCart.addItems(newItemCountMap);
	}

    private void closeCart(String cartId) {
		WorkerCart workerCart = getWorkerCart(cartId);
		workerCart.closeUserCart();
	}

	private void discardCart(String cartId) {
		WorkerCart workerCart = getWorkerCart(cartId);
		workerCart.discardCart();
		deleteCart(cartId);
	}

	/**
	 * 
	 * @param cartId
	 * @return
	 * There are few situations when Waiting time can not be calculated.
	 * 1. If user cart is not closed.
	 * 2. If worker cart is not assigned to any cart.
	 * 3. We are making call same time when worker session is closed and 
	 * releasing owner ship of worker carts.
	 */
	public long getCartWaitingTime(String cartId) {
		WorkerCart workerCart = getWorkerCart(cartId);
		if (workerCart.isClosed()) {
			return 5*60; // 5 mins.
		}
		WorkerSession workerSession = workerCart.getWorkerSession();
		if (null != workerSession && workerCart.isUserCartClosed()) {
			return workerSession.getWorkerCartWaitTime(cartId);
		}
		return -1;
	}

	public void deleteCart(String cartId) {
		WorkerCart workerCart = idToWorkerCartMap.remove(cartId);
		WorkerSession workerSession = workerCart.getWorkerSession();
		if (null != workerSession) {
			workerSession.removeWorkerCart(cartId);
		}
		if (workerCart != null) {
			// persist in db
		}
	}
}
