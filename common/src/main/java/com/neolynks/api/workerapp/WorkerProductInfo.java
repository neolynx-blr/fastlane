package com.neolynks.api.workerapp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkerProductInfo{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long barcode;
	private int count;
    protected boolean priceConflict;
    private int sequence;
}
