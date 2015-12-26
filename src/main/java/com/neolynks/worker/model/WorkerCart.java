package com.neolynks.worker.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.neolynks.common.model.client.ItemInfo;

public class WorkerCart {
	public enum WorkerCartStatus {
		OPEN, CLOSED;
	}

	private long id; // will take as userCartId
	private WorkerCartStatus status;
	private boolean isUserCartClosed;
	private long priority;
	private long waitingTime; // in seconds
	private long totalItems;
	private long processedItems;
	private Map<String, Long> processedItemCountMap;
	private Map<ItemInfo, Long> queuedItemCountMap;
	private Map<String, Long> inProcessedItemCountMap;
	private Set<String> allItems;
	private long load;
	
	public WorkerCart(long id) {
		this.id = id;
		status = WorkerCartStatus.OPEN;
		isUserCartClosed = false;
		priority = 0;
		waitingTime = 0;
		totalItems = 0;
		processedItems = 0;
		processedItemCountMap = new HashMap<String, Long>();
		queuedItemCountMap = new HashMap<ItemInfo, Long>();
		inProcessedItemCountMap = new HashMap<String, Long>();
		allItems = new HashSet<String>();
		load = 0;
	}
}
