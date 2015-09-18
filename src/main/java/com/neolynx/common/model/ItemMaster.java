package com.neolynx.common.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by nitesh.garg on 15-Sep-2015
 */

@Data
public class ItemMaster implements Serializable {
	
	private static final long serialVersionUID = 3297823532542422428L;
	
	private Long id;
	
	private Long barcode;
	private String itemCode;
	private Long versionId;

	private String name;
	private String tagline;
	private String description;

	private Double mrp;
	private Double price;

	private String imageJSON;

	private Integer discountType;
	private Double discountValue;

	//TODO Will need to add taxes etc. at some point

}
