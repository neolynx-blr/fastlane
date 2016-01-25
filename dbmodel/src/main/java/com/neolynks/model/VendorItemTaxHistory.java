package com.neolynks.model;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by nitesh.garg on Oct 26, 2015
 *
 */

@Data
@Entity
@Table(name = "vendor_item_tax_history")
public class VendorItemTaxHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "vendor_item_history_id")
	private Long vendorItemHistoryId;
	
	@Column(name = "tax_type")
	private Integer taxType;
	
	@Column(name = "tax_value")
	private Double taxValue;

}
