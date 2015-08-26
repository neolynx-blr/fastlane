package com.example.helloworld.core;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by nitesh.garg on 26-Aug-2015
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class InventoryResponse extends ResponseAudit {

	private int vendorId;
	private Long dataVersionId;
	
	private List<Product> itemsAdded;
	private List<Product> itemsUpdated;
	private List<Product> itemsRemoved;
	
}
