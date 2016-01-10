package com.neolynks.curator.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * Created by nitesh.garg on Oct 26, 2015
 *
 */

@Data
@Entity
@Table(name = "vendor_item_tax_master")
public class VendorItemTaxMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "vendor_item_master_id")
	private Long vendorItemMasterId;
	
	@Column(name = "tax_type")
	private Integer taxType;
	
	@Column(name = "tax_value")
	private Double taxValue;

}
