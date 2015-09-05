package com.example.helloworld.core;

import lombok.Data;

/* Indicates the data points that are sent to the device for each vendor-item combination for display purposes and final bill generation purposes. */

@Data
public class ItemResponse {

	private Long barcode;

	// private Long vendorId;
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
