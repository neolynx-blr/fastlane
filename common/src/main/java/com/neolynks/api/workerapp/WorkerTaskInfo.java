package com.neolynks.api.workerapp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkerTaskInfo implements Serializable {
	public enum TaskType {
		CREATE_CART, PICK_UP_PRODUCTS, NO_OPERATION;
	}

	private long Id;
	private TaskType taskType;
	private List<WorkerProductInfo> workerItems;
	private List<WorkerCartInfo> workerCartDetailsList;
}
