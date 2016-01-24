package com.neolynks.worker.dto;

import lombok.Data;

@Data
public class Worker {
	private String id;
	private String uniqueId; // need to think more on this.
	private long storeId;
}
