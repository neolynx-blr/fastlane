package com.neolynks.worker.model;

import lombok.Data;

@Data
public class Worker {
	private long id;
	private String uniqueId; // need to think more on this.
	private long storeId;
}
