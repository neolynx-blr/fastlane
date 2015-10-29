package com.neolynx.common.model;

/**
 * Created by nitesh.garg on 19-Sep-2015
 */
public class VendorRecordDetail extends ItemMaster {

	private static final long serialVersionUID = -6102017216752748745L;
	static final String[] INVENTORY_FILE_HEADER = { "id", "item_code", "version_id", "name", "description", "tag_line",
		"barcode", "mrp", "price", "image_json", "discount_type", "discount_value", "last_modified_on" };

	
	public Long uniqueId;
	
	public VendorRecordDetail(ItemMaster record) {
		super();
	}
	
	public String[] generateCSVRecord() {

		this.uniqueId = getUniqueId();
		
		String[] vendorInventoryRecord = new String[] { String.valueOf(this.getUniqueId()), this.getItemCode(),
				String.valueOf(this.getVersionId()), this.getName(), this.getDescription(), this.getTagline(),
				String.valueOf(this.getBarcode()), String.valueOf(this.getMrp()), String.valueOf(this.getPrice()),
				this.getImageJSON(), this.getDiscountJSON(), this.getTaxJSON() };
		
		return vendorInventoryRecord;

	}
	
	private Long getUniqueId() {
		/*
		 * TODO Generating this unique number per inventory item from vendor
		 * side. For now assuming we won't be processing more than records so
		 * fast for this id to collide and random() function to generate repeat
		 * fast enough.
		 */
		return Long.parseLong(System.currentTimeMillis() + "" + Math.random()*1000);
	}

}
