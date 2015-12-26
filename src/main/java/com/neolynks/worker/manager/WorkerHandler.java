package com.neolynks.worker.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.neolynks.common.model.client.ItemInfo;
import com.neolynks.common.model.client.WorkerTask;
import com.neolynks.worker.model.WorkerCart;
import com.neolynks.worker.model.WorkerSession;

public class WorkerHandler {
	Map<Long, WorkerCart> idToWorkerCartMap;
	Map<Long, WorkerSession> idToWorkerSessionMap;
	Map<Long, WorkerTask> sessionIdToActiveWorkerTaskMap;
	//Map<Long, WorkerSessions>;
	//Map<Long (WorkerSessions) id, Map<Long, CartDelta>> ;
	//Map<Long, WorkerTaskDetails>  activeWorkerTask;
	//Map<Long, Map<Long, CartDelta>> activeWorkerTaskToDeltaMap
	// Map <cartId, WorkerSessionsId>
	public WorkerHandler() {
		idToWorkerCartMap = new HashMap<Long, WorkerCart>();
	}
	
	public WorkerTask getWorkerTaskDetails(long workerSessionId) {
		// check if sessionIdToActiveWorkerTaskMap as task for workerSessionId and send it
		// generate workerTask with unique id.
		
		return null;
	}

	// Worker Cart
	public void addNewCart(long storeId, long cartId) {
		// pick up a worker based on load on workers for a given store and assign this cart
		// to that worker.
		WorkerCart workerCart = new WorkerCart(cartId);
		// workerSession.addWorkerCart(workerCart);
	}
	
	
	public void addCartDelta(long cartId, Map<ItemInfo, Long> newItemCountMap) {
		WorkerCart workerCart = new WorkerCart(cartId);
		//workerCart.mergeIntoQueudItems(queuedItemCountMap);
	}
	  
	 
}
