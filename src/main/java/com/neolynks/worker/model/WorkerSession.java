package com.neolynks.worker.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class WorkerSession {
	public enum SessionStatus {
		OPEN, PAUSED, CLOSED
	}
	private long id;
	private Worker worker;
	private Set<WorkerCart> workerCarts;
	private SessionStatus status;

	public WorkerSession(long id, Worker worker) {
		this.id = id;
		this.worker = worker;
		this.status = SessionStatus.OPEN;
		this.workerCarts = new HashSet<WorkerCart>();
	}
	// for now we are just using unique item count as load
	// this logic should need to be revised
	public long getLoad() {
		long load = 0;
		for (WorkerCart workerCart: workerCarts) {
			load += workerCart.getNewItemCountMap().size();
		}
		return load;
	}

}
