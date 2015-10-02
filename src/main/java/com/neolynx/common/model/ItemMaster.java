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

		System.out.println("Random value:" + StringUtilsCustom.newR.nextInt(1000));
		Long uniqueId = System.currentTimeMillis() + StringUtilsCustom.newR.nextInt(1000);
		System.out.println("Unique value:" + uniqueId);

		String[] vendorInventoryRecord = new String[] { String.valueOf(uniqueId), this.getItemCode(),
				String.valueOf(System.currentTimeMillis()), this.getName(), this.getDescription(), this.getTagline(),
				String.valueOf(this.getBarcode()), String.valueOf(this.getMrp()), String.valueOf(this.getPrice()),
				this.getImageJSON(), String.valueOf(this.getDiscountType()), String.valueOf(this.getDiscountValue()),
				String.valueOf(this.getLastModifiedOn()) };

		return vendorInventoryRecord;

	}

	public ItemMaster(CSVRecord record) {

		try {
			this.setId(Long.parseLong(record.get("id")));

			// TODO Think about this
			this.setVersionId(System.currentTimeMillis());

			this.setItemCode(record.get("item_code"));
			this.setBarcode(Long.parseLong(record.get("barcode")));

			this.setName(record.get("name"));
			this.setDescription(record.get("description"));
			this.setTagline(record.get("tag_line"));

			this.setImageJSON(record.get("image_json"));

			this.setMrp(Double.parseDouble(record.get("mrp")));
			this.setPrice(Double.parseDouble(record.get("price")));

			String discountType = record.get("discount_type");
			if (discountType == null || discountType.trim().equalsIgnoreCase("null")) {
				this.setDiscountType(null);
			} else {
				this.setDiscountType(Integer.parseInt(record.get("discount_type")));
			}

			String discountValue = record.get("discount_value");
			if (discountValue == null || discountValue.trim().equalsIgnoreCase("null")) {
				this.setDiscountValue(null);
			} else {
				this.setDiscountValue(Double.parseDouble(record.get("discount_value")));
			}

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

	public ItemMaster(Long id, Long barcode, String itemCode, Long versionId, String name, String tagline,
			String description, Double mrp, Double price, String imageJSON, Integer discountType, Double discountValue,
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
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.lastModifiedOn = lastModifiedOn;
	}

}
