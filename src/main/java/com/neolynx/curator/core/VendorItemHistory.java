package com.neolynx.curator.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Data;

/**
 * Created by nitesh.garg on 05-Sep-2015
 */

@Data
@Entity
@Table(name = "vendor_item_history")
@NamedQueries({ @NamedQuery(name = "com.example.helloworld.core.VendorItemHistory.findByVendor", query = "SELECT p FROM VendorItemHistory p where vendorId = :vendorId") })
public class VendorItemHistory {

	public VendorItemHistory(VendorItemMaster vendorItemMaster) {
		this.setBarcode(vendorItemMaster.getBarcode());
		this.setDescription(vendorItemMaster.getDescription());
		this.setDiscountType(vendorItemMaster.getDiscountType());
		this.setDiscountValue(vendorItemMaster.getDiscountValue());
		this.setImageJSON(vendorItemMaster.getImageJSON());
		this.setItemCode(vendorItemMaster.getItemCode());
		this.setMrp(vendorItemMaster.getMrp());
		this.setName(vendorItemMaster.getName());
		this.setPrice(vendorItemMaster.getPrice());
		this.setProductId(vendorItemMaster.getProductId());
		this.setTagLine(vendorItemMaster.getTagLine());
		this.setVendorId(vendorItemMaster.getVendorId());
		this.setVersionId(vendorItemMaster.getVersionId());
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "vendor_id", nullable = false)
	private Long vendorId;

	@Column(name = "version_id", nullable = false)
	private Long versionId;

	@Column(name = "item_code", nullable = false)
	private String itemCode;

	@Column(name = "barcode", nullable = false)
	private Long barcode;

	@Column(name = "mrp", nullable = false)
	private Double mrp;

	@Column(name = "price")
	private Double price;

	@Column(name = "image_json")
	private String imageJSON;

	@Column(name = "discount_type")
	private Integer discountType;

	@Column(name = "discount_value")
	private Double discountValue;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "tag_line")
	private String tagLine;

}
