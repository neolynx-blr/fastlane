package com.neolynks.worker.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.neolynks.util.RandomIdGenerator;
import com.neolynks.worker.model.WorkerCart;
import com.neolynks.worker.model.WorkerSession;
import com.neolynks.worker.model.WorkerTask;

public class WorkerHandler {

	// right now operations may not be thread safe
	Map<Long, Set<WorkerSession>> storeIdToWorkerSessionsMap;
	Map<Long, WorkerCart> idToWorkerCartMap;
	Map<Long, WorkerSession> idToWorkerSessionMap;
	Map<Long, WorkerTask> sessionIdToActiveWorkerTaskMap;
	Map<Long, Set<WorkerCart>> storeIdToUnAssignedWorkerCartsMap;

	public WorkerHandler() {
		storeIdToWorkerSessionsMap = new HashMap<Long, Set<WorkerSession>>();
		idToWorkerCartMap = new HashMap<Long, WorkerCart>();
		idToWorkerSessionMap = new HashMap<Long, WorkerSession>();
		sessionIdToActiveWorkerTaskMap = new HashMap<Long, WorkerTask>();
		storeIdToUnAssignedWorkerCartsMap = new HashMap<Long, Set<WorkerCart>>();
	}

	public void addWorkerSession(WorkerSession workerSession) {
		if (null == storeIdToWorkerSessionsMap.get(workerSession.getWorker().getStoreId())) {
			storeIdToWorkerSessionsMap.put(workerSession.getWorker().getStoreId(), new HashSet<WorkerSession>());
		}
		Set<WorkerSession> storeWorkerSessions = storeIdToWorkerSessionsMap.get(workerSession.getWorker().getStoreId());
		storeWorkerSessions.add(workerSession);
		// if there are pending worker carts to be assigned
		Set<WorkerCart> unAssignedWorkerCarts = storeIdToUnAssignedWorkerCartsMap.get(workerSession.getWorker().getStoreId());
		if (null != unAssignedWorkerCarts) {
			workerSession.getWorkerCarts().addAll(unAssignedWorkerCarts);
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
				workerCart.queuedItemsProcessed();
			}
		}
	}

	public void addNewCart(long storeId, long cartId) {
		// pick up a worker based on load on workers for a given store and assign this cart
		// to that worker.
		WorkerCart workerCart = new WorkerCart(cartId);
		assignCartToMostIdleWorkSessionInStore(storeId, workerCart);
	}

	public void addCartDelta(long cartId, Map<String, Integer> newItemCountMap) {
		WorkerCart workerCart = idToWorkerCartMap.get(cartId);
		workerCart.addItems(newItemCountMap);
	}

	public long closeCart(long cartId) {
		return 0;
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
	}

	private void assignCartToMostIdleWorkSessionInStore(long storeId, WorkerCart workerCart) {
		Set<WorkerSession> workerSessions = storeIdToWorkerSessionsMap.get(storeId);
		WorkerSession selectedWorkerSession = null;
		for (WorkerSession workerSession: workerSessions) {
			if (workerSession == null || workerSession.getStatus().equals(WorkerSession.SessionStatus.OPEN) &&
					workerSession.getLoad() > workerSession.getLoad()) {
				selectedWorkerSession = workerSession;
			}
		}
		if (null == selectedWorkerSession) {
			if (null == storeIdToUnAssignedWorkerCartsMap.get(storeId)) {
				storeIdToUnAssignedWorkerCartsMap.put(storeId, new HashSet<WorkerCart>());
			}
			storeIdToUnAssignedWorkerCartsMap.get(storeId).add(workerCart);
		} else {
			selectedWorkerSession.getWorkerCarts().add(workerCart);
			workerCart.setWorkerSession(selectedWorkerSession);
		}
		return;
	}
}
