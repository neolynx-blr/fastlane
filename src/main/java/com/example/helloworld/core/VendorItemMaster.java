package com.example.helloworld.core;

import java.util.Date;

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
 * Created by nitesh.garg on 25-Aug-2015
 */

@Data
@Entity
@Table(name = "vendor_item_master")
@NamedQueries({
		@NamedQuery(name = "com.example.helloworld.core.VendorItemMaster.findByVendor", query = "SELECT p FROM VendorItemMaster p where vendorId = :vendorId")})
public class VendorItemMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "vendor_id", nullable = false)
	private Long vendorId;

	@Column(name = "item_code", nullable = false)
	private String itemCode;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "version_id", nullable = false)
	private Long versionId;

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
	
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;
	
	@Column(name = "tag_line")
	private String tagLine;
	
	@Column(name = "created_on")
	private Date createdOn;
}
