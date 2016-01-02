package com.neolynks.worker.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
public class WorkerSession {

	public enum SessionStatus {
		OPEN, PAUSED, CLOSED
	}

	private long id;
	private Worker worker;
	private Map<Long, WorkerCart> idToworkerCartMap;
	private long createdOn;
	private Long closedOn;
	private SessionStatus status;

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
		this.idToworkerCartMap = new HashMap<Long, WorkerCart>();
		lastLoadUpdateTime = createdOn = System.currentTimeMillis();
	}

	public long getId() {
		return id;
	}

	public Worker getWorker() {
		return worker;
	}

	public long getCreatedOn() {
		return createdOn;
	}

	public Long getClosedOn() {
		if (isClosed()) {
			return closedOn;
		} else {
			// throw exception.
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
			// throw exception
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
		if (isOpen() && !isOverLoaded()) {
			idToworkerCartMap.put(workerCart.getId(), workerCart);
			workerCart.setWorkerSession(this);
		} else if (isOverLoaded()) {
			// throw exception
		} else {

		}
	}

	public synchronized void removeWorkerCarts(Collection<WorkerCart> workerCarts) {
		for(WorkerCart workerCart: workerCarts) {
			removeWorkerCart(workerCart.getId());
		}
	}

	public synchronized void removeWorkerCart(long workerCartId) {
		if (!isClosed()) {
			WorkerCart workerCart = idToworkerCartMap.get(workerCartId);
			if (null != workerCart) {
				idToworkerCartMap.remove(workerCartId);
				workerCart.setWorkerSession(null);
			}
		} else {
			// throw exception
		}
	}

	public synchronized long getWorkerCartWaitTime(long workerCartId) {
		if (isOpen()) {
			WorkerCart workerCart = idToworkerCartMap.get(workerCartId);
			if (null != workerCart) {
				return (getLoad(false)*2 + workerCart.getLoad(false)*5 + 5);
			}
		} else if(isClosed()) {
			// throw exception
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
			return allWorkerCarts;
		} else {
			// throw exception
		}
		return null;
	}

	@Override
	public boolean equals(Object object) {
		boolean result = true;
		if (object == null || object.getClass() != getClass()) {
			result = false;
		} else {
			WorkerSession workerSession = (WorkerSession) object;
			if (this.id != workerSession.getId()){
				result = false;
			}
		}
		return result;
	}
}
