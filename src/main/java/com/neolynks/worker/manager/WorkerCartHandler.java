package com.neolynks.worker.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.neolynks.worker.model.WorkerCart;
import com.neolynks.worker.model.WorkerSession;

/**
 * 
 * @author abhishekshukla
 * This class is thread safe implementation of 
 * Worker side cart corresponding to user side cart
 * Class is expected to have high concurrency as traffic increases.
 * Class expect for every new order there will be a new cart id.
 */
public class WorkerCartHandler {

	// add WorkerCartDao here 
	private ConcurrentHashMap<Long, WorkerCart> idToWorkerCartMap;
	private WorkerSessionHandler workerSessionHandler;

	public WorkerCartHandler(WorkerSessionHandler workerSessionHandler) {
		this.workerSessionHandler = workerSessionHandler;
		idToWorkerCartMap = new ConcurrentHashMap<Long, WorkerCart>();
	}

	private WorkerCart getWorkerCart(long cartId) {
		WorkerCart workerCart = idToWorkerCartMap.get(cartId);
		if (null == workerCart) {
			// throw exception
		}
		return workerCart;
	}

	public void initWorkerCart(long cartId, long storeId) {
		WorkerCart workerCart = new WorkerCart(cartId, storeId);
		if (null != idToWorkerCartMap.putIfAbsent(cartId, workerCart) ) {
			workerSessionHandler.addWorkerCartForWorkerSessionAssignment(workerCart);
		} else {
			// throw exception
		}
	}

	public void addCartDelta(long cartId, Map<Long, Integer> newItemCountMap) {
		WorkerCart workerCart = getWorkerCart(cartId);
		workerCart.addItems(newItemCountMap);
	}

	public long closeCart(long cartId) {
		WorkerCart workerCart = getWorkerCart(cartId);
		workerCart.closeUserCart();
		return getCartWaitingTime(cartId);
	}

	public void discardCart(long cartId) {
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
	public long getCartWaitingTime(long cartId) {
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

	public void deleteCart(long cartId) {
		WorkerCart workerCart = idToWorkerCartMap.remove(cartId);
		if (workerCart != null) {
			// persist in db
		}
	}
}
