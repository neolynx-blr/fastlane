package com.neolynx.common.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.csv.CSVRecord;

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
	
	private Date lastModifiedOn;

	// TODO Will need to add taxes etc. at some point

	public String[] generateCSVRecord() {

		/*
		 * TODO Generating this unique number per inventory item from vendor
		 * side. For now assuming we won't be processing more than records so
		 * fast for this id to collide and random() function to generate repeat
		 * fast enough.
		 */
		Long uniqueId = Long.parseLong(System.currentTimeMillis() + "" + Math.random() * 1000);

		String[] vendorInventoryRecord = new String[] { String.valueOf(uniqueId), this.getItemCode(),
				String.valueOf(this.getVersionId()), this.getName(), this.getDescription(), this.getTagline(),
				String.valueOf(this.getBarcode()), String.valueOf(this.getMrp()), String.valueOf(this.getPrice()),
				this.getImageJSON(), String.valueOf(this.getDiscountType()), String.valueOf(this.getDiscountValue()) };

		return vendorInventoryRecord;

	}
	
	@SuppressWarnings("deprecation")
	public ItemMaster(CSVRecord record) {
		
		this.setId(Long.parseLong(record.get("id")));
		this.setVersionId(Long.parseLong(record.get("version_id")));
		this.setItemCode(record.get("item_code"));
		this.setBarcode(Long.parseLong(record.get("barcode")));

		this.setName(record.get("name"));
		this.setDescription(record.get("description"));
		this.setTagline(record.get("tag_line"));

		this.setImageJSON(record.get("image_json"));

		this.setMrp(Double.parseDouble(record.get("mrp")));
		this.setPrice(Double.parseDouble(record.get("price")));
		this.setDiscountType(Integer.parseInt(record.get("discount_type")));
		this.setDiscountValue(Double.parseDouble(record.get("discount_value")));
		
		//TODO
		this.setLastModifiedOn(new java.util.Date(Date.parse(record.get("last_modified_on"))));
		
	}

	public ItemMaster() {
	}

}
