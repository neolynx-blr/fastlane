package com.example.helloworld.core;

import lombok.Data;

@Data
public class ItemResponse {

	private Long barcode;

//	private Long vendorId;
	private Long versionId;
	
	private Long productId;
	private String itemCode;
	
	private String name;
	private String tagline;
	private String description;

	private Double mrp;
	private Double price;	

	private String imageJSON;
	
	private int discountType;
	private Double discountValue;

}
