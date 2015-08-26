package com.example.helloworld.core;

import lombok.Data;

@Data
public class Inventory {

	private Long itemId;
	private Long barcode;

	private int vendorId;
	private Long productId;

	private String name;
	private String tagline;
	private String description;

	private Double mrp;
	private Double price;
	private Long versionId;

	private String imageJSON;

}
