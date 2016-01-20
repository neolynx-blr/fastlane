package com.neolynks.worker.dto;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.neolynks.util.RandomIdGenerator;
import com.neolynks.worker.exception.WorkerException;
import com.neolynks.worker.exception.WorkerException.WORKER_SESSION_ERROR;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 *
 * @author abhishekshukla
 * Class is thread safe implementation of WorkerSession.
 * We have preferred here to use synchronized over concurrent containers
 * because on a single WorkerSession we are not expecting many concurrent operations.
 *
 *
 * Class need to be improved. There should be a threshold on number of carts a worker
 * should be on a given time.
 *
 * There are part where strict synchronization is not applied. For example there is
 * no check on load while adding a new cart.
 *
 */
@EqualsAndHashCode(of = "id")
public class WorkerSession {

	public enum SessionStatus {
		OPEN, PAUSED, CLOSED
	}

    @Getter
	private long id;
    @Getter
	private Worker worker;
	private Map<String, WorkerCart> idToworkerCartMap;
    @Getter
	private long createdOn;
	private Long closedOn;
	private SessionStatus status;
	private WorkerTask currentWorkerTask;
	private Long lastWorkerTaskId;

	// Getting load is going to be costly operation
	// if not now then for sure in future.
	public static long MAX_ALLOWED_LOAD = 50;
	public static long LOAD_REFRESH_TIME = 60*1000L; // 60 seconds.
	private Long load;
	private long lastLoadUpdateTime;

	public WorkerSession(long id, Worker worker) {
		this.id = id;
		this.worker = worker;
		this.status = SessionStatus.OPEN;
		// It doesn't make much sense to use concurrent hash map here
		// 1. I am not expecting lots of concurrency for single WorkerSession
		// 2. Any way many operations iterating workerCarts so we need to handle
		//    concurrent modification exception
		this.idToworkerCartMap = new HashMap<String, WorkerCart>();
		lastLoadUpdateTime = createdOn = System.currentTimeMillis();
	}

	public Long getClosedOn() {
		if (isClosed()) {
			return closedOn;
		}
		return null;
	}

	public synchronized long getLoad(boolean forceRefresh) {
		if (isOpen()) {
			if (null == load || System.currentTimeMillis() - lastLoadUpdateTime > LOAD_REFRESH_TIME) {
				load = 0L;
				for (WorkerCart workerCart: idToworkerCartMap.values()) {
					load += workerCart.getLoad(false);
				}
			}
			return load;
		} else {
			load = 0L;
		}
		return load;
	}

	public boolean isOverLoaded() {
		if (getLoad(false) >= MAX_ALLOWED_LOAD) {
			return true;
		}

		return false;
	}

	public synchronized boolean isClosed() {
		if (status == SessionStatus.CLOSED) {
			return true;
		}
		return false;
	}

	public synchronized void open() {
		status = SessionStatus.OPEN;
	}

	public synchronized boolean isOpen() {
		if (status == SessionStatus.OPEN) {
			return true;
		}
		return false;
	}

	public synchronized void pause() {
		if (isOpen()) {
			status = SessionStatus.PAUSED;
		} else {
			throw new WorkerException(isPaused() ? WORKER_SESSION_ERROR.SESSION_PAUSED: WORKER_SESSION_ERROR.SESSION_CLOSED);
		}
	}

	public synchronized void restart() {
		if (isPaused()) {
			status = SessionStatus.OPEN;
		}
	}
	
	public synchronized boolean isPaused() {
		if (status == SessionStatus.PAUSED) {
			return true;
		}
		return false;
	}

	public synchronized void addWorkerCarts(Collection<WorkerCart> workerCarts) {
		for(WorkerCart workerCart: workerCarts) {
			addWorkerCart(workerCart);
		}
	}

	public synchronized void addWorkerCart(WorkerCart workerCart) {
        boolean isOverLoaded =  isOverLoaded();
		if (isOpen() && !isOverLoaded) {
			idToworkerCartMap.put(workerCart.getId(), workerCart);
			workerCart.setWorkerSession(this);
			load += workerCart.getLoad(false);
		} else if (isOverLoaded) {
			throw new WorkerException(WORKER_SESSION_ERROR.WORKER_OVERLOADED);
		} else {
			throw new WorkerException(isPaused() ? WORKER_SESSION_ERROR.SESSION_PAUSED: WORKER_SESSION_ERROR.SESSION_CLOSED);
		}
	}

	public synchronized void removeWorkerCarts(Collection<WorkerCart> workerCarts) {
		for(WorkerCart workerCart: workerCarts) {
			removeWorkerCart(workerCart.getId());
		}
	}

	public synchronized void removeWorkerCart(String workerCartId) {
		WorkerCart workerCart = idToworkerCartMap.get(workerCartId);
		if (null != workerCart) {
			idToworkerCartMap.remove(workerCartId);
			workerCart.setWorkerSession(null);
			load -= workerCart.getLoad(false);
		}
	}

	public synchronized long getWorkerCartWaitTime(String workerCartId) {
		if (isOpen()) {
			WorkerCart workerCart = idToworkerCartMap.get(workerCartId);
			if (null != workerCart) {
				return (getLoad(false)*2 + workerCart.getLoad(false)*5 + 5);
			}
		}
		// in case state is paused or workerCart is reassigned to other workerSession.
		return -1;
	}

	public synchronized Collection<WorkerCart> closeAndReleaseWorkerCarts() {
		if (!isClosed()) {
			status = SessionStatus.CLOSED;
			Collection<WorkerCart> allWorkerCarts  = idToworkerCartMap.values();
			removeWorkerCarts(allWorkerCarts);
			idToworkerCartMap.clear();
			closedOn = System.currentTimeMillis();
			return allWorkerCarts;
		} 
		// no need to throw exception for calling close multiple times
		return null;
	}

	/**
	 * Ideally there should be a factory call for creation of object
	 * We can improve it in future
	 * @return
	 */
	public synchronized WorkerTask getWorkerTask() {
		if (null != currentWorkerTask) {
			return currentWorkerTask;
		}
		lastWorkerTaskId = RandomIdGenerator.getInstance().generateId();
		WorkerTask workerTask = new WorkerTask(RandomIdGenerator.getInstance().generateId());
		boolean isAnyCartClosed = false;
		boolean itemCountZero = true;
		for (WorkerCart workerCart: idToworkerCartMap.values()) {
			if(workerCart.isUserCartClosed()) {
				isAnyCartClosed = true;
			}
			if (workerCart.hasAnyItemQueued()) {
				itemCountZero = false;
			}
		}

		workerTask.setWorkerCarts(new HashSet<WorkerCart>(idToworkerCartMap.values()));
		Map<Long, Integer> items = new HashMap<Long, Integer>();
		if (isAnyCartClosed) {
			workerTask.setTaskType(WorkerTask.TaskType.CREATE_CART);
		} else if (!itemCountZero) {
			workerTask.setTaskType(WorkerTask.TaskType.PICK_UP_PRODUCTS);
			for (WorkerCart workerCart: idToworkerCartMap.values()) {
				workerCart.queueItemsForProcessing();
				Map<Long, Integer> pendingItemsForProcessing = workerCart.getPendingItemMap();
				for (Entry<Long, Integer> entry : pendingItemsForProcessing.entrySet()) {
					if(items.get(entry.getKey()) != null) {
						items.put(entry.getKey(), items.get(entry.getKey()) + entry.getValue());
					} else {
						items.put(entry.getKey(), entry.getValue());
					}
				}
			}
			workerTask.setItems(items);
		} else {
			workerTask.setTaskType(WorkerTask.TaskType.NO_OPERATION);
		}

		return workerTask;
	}

	public synchronized void completeWorkerTask(Long workTaskId) {
		if (lastWorkerTaskId != workTaskId && lastWorkerTaskId < workTaskId) {
			throw new WorkerException(WORKER_SESSION_ERROR.INVALID_WORKER_TASK);
		}
		else if (lastWorkerTaskId == workTaskId && null != currentWorkerTask) {
			if (currentWorkerTask.getTaskType() == WorkerTask.TaskType.PICK_UP_PRODUCTS) {
				for (WorkerCart workerCart: idToworkerCartMap.values()) {
					workerCart.pendingItemsProcessed();
				}
			} else if (currentWorkerTask.getTaskType() == WorkerTask.TaskType.CREATE_CART) {
				// we can remove carts here but better let it be there till worker remove
				//himself
			}
		}
	}

}
