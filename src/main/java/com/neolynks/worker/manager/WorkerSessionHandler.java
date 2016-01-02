package com.neolynks.worker.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.neolynks.util.RandomIdGenerator;
import com.neolynks.worker.model.WorkerCart;
import com.neolynks.worker.model.WorkerSession;
import com.neolynks.worker.model.WorkerTask;
/**
 * 
 * @author abhishekshukla
 * Right now Workercart and WorkerSession operation both are 
 * handler by this class. If it makes sense in future we should 
 * create two manager classes to handle operations separately
 */
public class WorkerSessionHandler {

	ConcurrentHashMap<Long, ConcurrentHashMap<Long, WorkerSession>> storeIdToWorkerSessionsMap;
	Map<Long, WorkerTask> sessionIdToActiveWorkerTaskMap;
	private ConcurrentLinkedQueue<WorkerCart> unassignedWorkerCartQueue ;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	private volatile boolean isShutDown = false;

	public void shutDown() {
		isShutDown = true;
		executorService.shutdown();
	}

	private class WorkerCartProcessor implements Runnable
    {  	 
        public void run()
        {
     	   while(!isShutDown)
     	   {
        	try 
        	{
        		WorkerCart workerCart = null;
        		if (null != (workerCart = unassignedWorkerCartQueue.poll())) {
        			WorkerSession workerSession = getMostIdleWorkerSession(workerCart.getStoreId());
        			workerSession.addWorkerCart(workerCart);
        		}
        	} 
        	catch(Exception e)
        	{
        		// log exception and ignore.
        	}
    	   }
        }
    }

	public WorkerSessionHandler() {
		storeIdToWorkerSessionsMap = new ConcurrentHashMap<Long, ConcurrentHashMap<Long, WorkerSession>>();
		sessionIdToActiveWorkerTaskMap = new HashMap<Long, WorkerTask>();
		executorService.execute(new WorkerCartProcessor());
	}

	public void addWorkerCartForWorkerSessionAssignment(WorkerCart workerCart) {
		unassignedWorkerCartQueue.add(workerCart);
	}

	private WorkerSession getMostIdleWorkerSession(long storeId) {
		Map<Long, WorkerSession> workerSessions = storeIdToWorkerSessionsMap.get(storeId);
		if (null == workerSessions || 0 == workerSessions.size()) {
			return null;
		}
	
		WorkerSession selectedWorkerSession = null;
		for (WorkerSession workerSession: workerSessions.values()) {
			if (workerSession.isOpen() && !workerSession.isOverLoaded() 
					&& (null == selectedWorkerSession || selectedWorkerSession.getLoad(false) > workerSession.getLoad(false))) {
				selectedWorkerSession = workerSession;
			}
		}
		return selectedWorkerSession;
	}

	/*public void addWorkerSession(WorkerSession workerSession) {
		// following code is not at all thread safe (many thread) issues.
		if (null == storeIdToWorkerSessionsMap.get(workerSession.getWorker().getStoreId())) {
			storeIdToWorkerSessionsMap.put(workerSession.getWorker().getStoreId(), new HashSet<WorkerSession>());
		}
		Set<WorkerSession> storeWorkerSessions = storeIdToWorkerSessionsMap.get(workerSession.getWorker().getStoreId());
		storeWorkerSessions.add(workerSession);
		// if there are pending worker carts to be assigned
		Set<WorkerCart> unAssignedWorkerCarts = storeIdToUnAssignedWorkerCartsMap.get(workerSession.getWorker().getStoreId());
		if (null != unAssignedWorkerCarts) {
			workerSession.getWorkerCarts().addAll(unAssignedWorkerCarts);
			for(WorkerCart workerCart: unAssignedWorkerCarts)
			{
				workerCart.setWorkerSession(workerSession);
			}
		}
		storeIdToUnAssignedWorkerCartsMap.remove(workerSession.getWorker().getStoreId());
	}

	public WorkerTask getWorkerTaskDetails(long workerSessionId) {
		WorkerSession workerSession = idToWorkerSessionMap.get(workerSessionId);
		if (null != sessionIdToActiveWorkerTaskMap.get(workerSessionId)) {
			return sessionIdToActiveWorkerTaskMap.get(workerSessionId);
		}
		WorkerTask workerTask = new WorkerTask(RandomIdGenerator.getInstance().generateId());
		Set<Long> closedUserCarts = new HashSet<Long>();
		Map<String, Integer> items = new HashMap<String, Integer>();
		for (WorkerCart workerCart: workerSession.getWorkerCarts()) {
			if(workerCart.isUserCartClosed()) {
				closedUserCarts.add(workerCart.getId());
			}
			items.putAll(workerCart.getNewItemCountMap());
		}
		workerTask.setWorkerCarts(workerSession.getWorkerCarts());

		if (0 != closedUserCarts.size()) {
			workerTask.setTaskType(WorkerTask.TaskType.CREATE_CART);
		} else if (0 != items.size()) {
			workerTask.setItems(items);
			workerTask.setTaskType(WorkerTask.TaskType.PICK_UP_PRODUCTS);
			for (WorkerCart workerCart: workerSession.getWorkerCarts()) {
				workerCart.queueItemsForProcessing();
			}
		} else {
			workerTask.setTaskType(WorkerTask.TaskType.NO_OPERATION);
		}

		return workerTask;
	}

	public void completeWorkerTask(long workerSessionId, long workerTaskId) {
		WorkerSession workerSession = idToWorkerSessionMap.get(workerSessionId);
		WorkerTask workerTask = sessionIdToActiveWorkerTaskMap.get(workerSessionId);
		if (workerTask.getId() != workerSessionId) {
			// something wrong last server task doesn't match server side.
		}
		sessionIdToActiveWorkerTaskMap.remove(workerSessionId);
		if (workerTask.getTaskType() == WorkerTask.TaskType.PICK_UP_PRODUCTS) {
			for (WorkerCart workerCart: workerSession.getWorkerCarts()) {
				workerCart.pendingItemsProcessed();
			}
		}
	}

	public void pauseWorkerSession(long workerSesssionId) {
		WorkerSession workerSession = idToWorkerSessionMap.get(workerSesssionId);
		workerSession.setStatus(WorkerSession.SessionStatus.PAUSED);
	}

	public void terminateWorkerSession(long workerSesssionId) {
		WorkerSession workerSesssion = idToWorkerSessionMap.get(workerSesssionId);
		long storeId = workerSesssion.getWorker().getStoreId();
		Set<WorkerCart> workerCarts = workerSesssion.getWorkerCarts();
		for(WorkerCart workerCart : workerCarts) {
			assignCartToMostIdleWorkSessionInStore(storeId, workerCart);
		}
		// find if there is any active task then remove it.
		sessionIdToActiveWorkerTaskMap.remove(storeId);
		idToWorkerSessionMap.remove(workerSesssionId);
	}

	public void reactivateWorkerSession(long workerSesssionId) {
		WorkerSession workerSession = idToWorkerSessionMap.get(workerSesssionId);
		if (workerSession.getStatus().equals(WorkerSession.SessionStatus.PAUSED)) {
			workerSession.setStatus(WorkerSession.SessionStatus.OPEN);
		}
		else {
			// throw exception and client should recreate worker session.
		}
	}*/
}
