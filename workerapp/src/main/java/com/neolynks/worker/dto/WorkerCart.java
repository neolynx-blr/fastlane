package com.neolynks.worker.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.neolynks.util.Pair;
import com.neolynks.worker.exception.WorkerException;
import com.neolynks.worker.exception.WorkerException.WORKER_CART_ERROR;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 *
 * @author abhishekshukla
 * WorkerCart is thread safe class to handle worker side cart
 * operations.
 *
 */
@EqualsAndHashCode(of = "id")
public class WorkerCart {
	public enum WorkerCartStatus {
		OPEN, CLOSED, DISCARDED
	}

	/**
	 *  Improve concurrency in code.
	 *  Improve load calculation
	 *  WorkerCart ----> WorkerTask, WorkerSession, CartManager/WorkerSessionManager.
	 *  This class is expected to have moderate level of concurrency so improvement is
	 *  required.
	 */
    @Getter
	private String id; // will take as userCartId
    @Getter
	private long storeId;
	private WorkerCartStatus status;
	private volatile boolean isUserCartClosed;
    @Getter
	private Long createdOn; // milliSeconds.
	private Long closedOn;  // milliSeconds.
	private Map<String, Integer> processedItemCountMap;
	private Map<String, Integer> queuedItemCountMap;
	private Map<String, Integer> pendingItemCountMap;
	private WorkerSession workerSession;

	// Getting load is going to be costly operation
	// if not now then for sure in future.
	public static long MAX_ALLOWED_LOAD = 50;
	public static long LOAD_REFRESH_TIME = 5*1000L; // 5 seconds.
	private Long load;
	private long lastLoadUpdateTime;

	public WorkerCart(String id, long storeId) {
		this.id = id;
		this.storeId = storeId;
		status = WorkerCartStatus.OPEN;
		isUserCartClosed = false;
		processedItemCountMap = new HashMap<String, Integer>();
		queuedItemCountMap = new HashMap<String, Integer>();
		pendingItemCountMap = new HashMap<String, Integer>();
		lastLoadUpdateTime = createdOn = System.currentTimeMillis();
	}

	public Long getPriority() {
		// decide some logic of calculating priority.
		// first come first serve for now.
		return createdOn;
	}

	public Long getClosedOn() {
		if (isClosed()) {
			return closedOn;
		}
		return null;
	}

	// for now we are just using unique item count as load
	// this logic should need to be revised
	// we don't much care about accurate values so not making it synchronized.
	public long getLoad(boolean forceRefresh) {
		if (isOpen()) {
			if (forceRefresh || null == load || System.currentTimeMillis() - lastLoadUpdateTime > LOAD_REFRESH_TIME) {
				load = (long) queuedItemCountMap.size();
			}
			return load;
		} else {
			load = 0L;
		}
		return load;
	}

	public synchronized WorkerSession getWorkerSession() {
		return workerSession;
	}

	public synchronized void setWorkerSession(WorkerSession workerSession) {
		if (isOpen()) {
			if (this.workerSession != workerSession) {
				// when cart is reassigned to new worker session
				addItems(processedItemCountMap);
				processedItemCountMap.clear();
			}
			this.workerSession = workerSession;
		} else {
			throw new WorkerException(WORKER_CART_ERROR.CART_CLOSED);
		}
	}

	public synchronized void closeUserCart() {
		isUserCartClosed = true;
	}

	public synchronized boolean isUserCartClosed() {
		return isUserCartClosed;
	}

	public synchronized void discardCart() {
		status = WorkerCartStatus.DISCARDED;
	}

	public synchronized boolean isClosed() {
		return status == WorkerCartStatus.CLOSED;
	}

	public synchronized boolean isDiscarded() {
		return status == WorkerCartStatus.DISCARDED;
	}

	public synchronized boolean isOpen() {
		return status == WorkerCartStatus.OPEN;
	}

	public synchronized void addItems(Map<String, Integer> items) {
		if (isOpen()) {
			for( Entry<String, Integer> entry : items.entrySet()) {
				if (queuedItemCountMap.get(entry.getKey()) != null) {
					queuedItemCountMap.put(entry.getKey(), entry.getValue() + queuedItemCountMap.get(entry.getKey()));
				}
				else {
					queuedItemCountMap.put(entry.getKey(), entry.getValue());
				}
			}
		} else {
			throw new WorkerException(WORKER_CART_ERROR.CART_CLOSED);
		}
	}

	public synchronized boolean hasAnyItemQueued() {
		return queuedItemCountMap.size() != 0;
	}

	public synchronized void pendingItemsProcessed() {
		for( Entry<String, Integer> entry : pendingItemCountMap.entrySet()) {
			if (processedItemCountMap.get(entry.getKey()) != null) {
				processedItemCountMap.put(entry.getKey(), entry.getValue() + processedItemCountMap.get(entry.getKey()));
			}
			else {
				processedItemCountMap.put(entry.getKey(), entry.getValue());
			}
		}
		if (isUserCartClosed) {
			status = WorkerCartStatus.CLOSED;
			closedOn = System.currentTimeMillis();
		}
		pendingItemCountMap.clear();
	}

	public synchronized void queueItemsForProcessing() {
		if (isOpen()) {
			pendingItemCountMap = queuedItemCountMap;
			queuedItemCountMap.clear();
		} else {
			throw new WorkerException(WORKER_CART_ERROR.CART_CLOSED);
		}
	}

	public synchronized Map<String, Integer> getPendingItemMap() {
		return new HashMap<String, Integer>(pendingItemCountMap);
	}

	public synchronized Map<String, Integer> getProcessedItemMap() {
		return new HashMap<String, Integer>(processedItemCountMap);
	}

	public synchronized Pair<Integer, Integer> getTotalAndProcessedItems() {
		Set<String> items = new HashSet<String>(processedItemCountMap.keySet());
		items.addAll(pendingItemCountMap.keySet());
		Integer totalItems = items.size();
		items.removeAll(pendingItemCountMap.keySet());
		Integer processedItems = items.size();
		return new Pair<Integer, Integer>(totalItems, processedItems);
	}

}
