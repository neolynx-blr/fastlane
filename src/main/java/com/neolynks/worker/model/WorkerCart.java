package com.neolynks.worker.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.Data;

import com.neolynks.util.Pair;

@Data
public class WorkerCart {
	public enum WorkerCartStatus {
		OPEN, CLOSED;
	}

	// TO DO
	/**
	 *  Improve concurrency in code.
	 *  Improve load calculation
	 */
	private long id; // will take as userCartId
	private WorkerCartStatus status;
	private boolean isUserCartClosed;
	private long priority;
	private long waitingTime; // in seconds
	private long createdOn;
	private long closedOn;
	private Map<String, Integer> processedItemCountMap;
	private Map<String, Integer> newItemCountMap;
	private Map<String, Integer> queuedItemCountMap;
	private Set<String> allItems;
	private WorkerSession workerSession;

	public WorkerCart(long id) {
		this.id = id;
		status = WorkerCartStatus.OPEN;
		isUserCartClosed = false;
		priority = 0;
		waitingTime = 0;
		processedItemCountMap = new HashMap<String, Integer>();
		newItemCountMap = new HashMap<String, Integer>();
		queuedItemCountMap = new HashMap<String, Integer>();
		allItems = new HashSet<String>();
		createdOn = System.currentTimeMillis();
	}

	@Override
	public boolean equals(Object object) {
		boolean result = true;
		if (object == null || object.getClass() != getClass()) {
			result = false;
		} else {
			WorkerCart workerCart = (WorkerCart) object;
			if (this.id != workerCart.getId()){
				result = false;
			}
		}
		return result;
	}

	public synchronized void addItems(Map<String, Integer> items) {
		for( Entry<String, Integer> entry : items.entrySet()) {
			if (newItemCountMap.get(entry.getKey()) != null) {
				newItemCountMap.put(entry.getKey(), entry.getValue() + newItemCountMap.get(entry.getKey()));
			}
			else {
				newItemCountMap.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public synchronized void queuedItemsProcessed() {
		for( Entry<String, Integer> entry : queuedItemCountMap.entrySet()) {
			if (processedItemCountMap.get(entry.getKey()) != null) {
				processedItemCountMap.put(entry.getKey(), entry.getValue() + processedItemCountMap.get(entry.getKey()));
			}
			else {
				processedItemCountMap.put(entry.getKey(), entry.getValue());
			}
		}
		if (isUserCartClosed) {
			status = WorkerCartStatus.CLOSED;
		}
		queuedItemCountMap.clear();
	}

	public synchronized void queueItemsForProcessing() {
		queuedItemCountMap = newItemCountMap;
		newItemCountMap = new HashMap<String, Integer>();
	}

	public Pair<Integer, Integer> getTotalAndProcessedItems() {
		Set<String> items = new HashSet<String>(processedItemCountMap.keySet());
		items.addAll(queuedItemCountMap.keySet());
		Integer totalItems = allItems.size();
		items.removeAll(queuedItemCountMap.keySet());
		Integer processedItems = allItems.size();
		return new Pair<Integer, Integer>(totalItems, processedItems);
	}

	public void closeCart() {
		isUserCartClosed = true;
	}
}
