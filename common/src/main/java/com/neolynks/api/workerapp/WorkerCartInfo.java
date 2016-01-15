package com.neolynks.api.workerapp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkerCartInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public enum Status {
		OPEN, CLOSED, DISCARDED;
	}
	private long id;
    private String customerId;
    private String customerName;
	private Status status;
	private long priority;
	private long waitingTime; // in seconds
	private long totalItems;
	private long processedItems;	
}
