package com.neolynx.common.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import org.apache.commons.csv.CSVRecord;

import com.neolynx.curator.util.StringUtilsCustom;

/**
 * Created by nitesh.garg on 15-Sep-2015
 */

@Data
public class ItemMaster implements Serializable {

	private static final long serialVersionUID = 3297823532542422428L;

	private Long id;

	private String barcode;
	private String itemCode;
	private Long versionId;

	private String name;
	private String tagline;
	private String description;

	private Double mrp;
	private Double price;

	private String imageJSON;

	private String taxJSON;
	private String discountJSON;

	private Date lastModifiedOn;

	// TODO Will need to add taxes etc. at some point

	public String[] generateCSVRecord() {

		/*
		 * TODO Generating this unique number per inventory item from vendor
		 * side. For now assuming we won't be processing more than records so
		 * fast for this id to collide and random() function to generate repeat
		 * fast enough.
		 */

		System.out.println("Random value:" + StringUtilsCustom.newR.nextInt(1000));
		Long uniqueId = System.currentTimeMillis() + StringUtilsCustom.newR.nextInt(1000);
		System.out.println("Unique value:" + uniqueId);

		String[] vendorInventoryRecord = new String[] { String.valueOf(uniqueId), this.getItemCode(),
				String.valueOf(System.currentTimeMillis()), this.getName(), this.getDescription(), this.getTagline(),
				String.valueOf(this.getBarcode()), String.valueOf(this.getMrp()), String.valueOf(this.getPrice()),
				this.getImageJSON(), this.getDiscountJSON(), this.getTaxJSON(),
				String.valueOf(this.getLastModifiedOn()) };

		return vendorInventoryRecord;

	}

	public ItemMaster(CSVRecord record) {

		try {
			this.setId(Long.parseLong(record.get("id")));

			// TODO Think about this
			this.setVersionId(System.currentTimeMillis());

			this.setItemCode(record.get("item_code"));
			this.setBarcode(record.get("barcode"));

			this.setName(record.get("name"));
			this.setDescription(record.get("description"));
			this.setTagline(record.get("tag_line"));

			this.setImageJSON(record.get("image_json"));

			this.setMrp(Double.parseDouble(record.get("mrp")));
			this.setPrice(Double.parseDouble(record.get("price")));

			this.setTaxJSON(record.get("tax_json"));
			this.setDiscountJSON(record.get("discount_json"));

			// TODO
			String lastModifiedOn = record.get("last_modified_on");
			if (lastModifiedOn == null || lastModifiedOn.trim().equalsIgnoreCase("null")) {
				this.setLastModifiedOn(null);
			} else {
				this.setLastModifiedOn(new Date(System.currentTimeMillis()));
			}
		} catch (NumberFormatException nfe) {

		} catch (Exception e) {
			
		}

	}

	public ItemMaster() {
	}

	public ItemMaster(Long id, String barcode, String itemCode, Long versionId, String name, String tagline,
			String description, Double mrp, Double price, String imageJSON, String discountJSON, String taxJSON,
			Date lastModifiedOn) {
		super();
		this.id = id;
		this.barcode = barcode;
		this.itemCode = itemCode;
		this.versionId = versionId;
		this.name = name;
		this.tagline = tagline;
		this.description = description;
		this.mrp = mrp;
		this.price = price;
		this.imageJSON = imageJSON;
		this.discountJSON = discountJSON;
		this.taxJSON = taxJSON;
		this.lastModifiedOn = lastModifiedOn;
	}

}
