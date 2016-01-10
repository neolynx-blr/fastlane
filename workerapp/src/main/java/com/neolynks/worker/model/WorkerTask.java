package com.neolynks.worker.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class WorkerTask {
	public enum TaskType {
		CREATE_CART, PICK_UP_PRODUCTS, NO_OPERATION;
	}

	private long id;
	private TaskType taskType;
	private Map<Long, Integer> items;
	private Set<WorkerCart> workerCarts;

	public WorkerTask(long id) {
		this.id = id;
		taskType = TaskType.NO_OPERATION;
	}
}
