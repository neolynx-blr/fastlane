package com.neolynks.model;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by nitesh.garg on Oct 26, 2015
 *
 */

@Data
@Entity
@Table(name = "vendor_item_discount_master")
public class VendorItemDiscountMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "vendor_item_master_id")
	private Long vendorItemMasterId;

	@Column(name = "discount_type")
	private Integer discountType;

	@Column(name = "discount_value")
	private Double discountValue;
	
	@Column(name = "discount_required_count")
	private Integer requiredCountForDiscount;
	
	@Column(name = "discounted_item_code")
	private String discountedItemCode;
	
}