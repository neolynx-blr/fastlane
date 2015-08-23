package com.example.helloworld.core;

import java.util.List;

import lombok.Data;

@Data
public class Inventory {
	
	private Long vendorId;
	private String dataVersionId;
	
	private List<Product> productsAdded;
	private List<Product> productsUpdated;
	private List<Product> productsRemoved;
	

}
