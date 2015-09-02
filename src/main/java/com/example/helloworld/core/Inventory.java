package com.example.helloworld.core;

import lombok.Data;

@Data
public class Inventory {

	private String itemCode;
	private Long barcode;

	private Long vendorId;
	private Long productId;

	private String name;
	private String tagline;
	private String description;

	private Double mrp;
	private Double price;
	private Long versionId;

	private String imageJSON;
	
	private int discountType;
	private Double discountValue;

}
