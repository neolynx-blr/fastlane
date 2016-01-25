package com.neolynks.model;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by nitesh.garg on Oct 26, 2015
 *
 */

@Data
@Entity
@Table(name = "inventory_tax_master")
public class InventoryTaxMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "inventory_master_id")
	private Long inventoryMasterId;
	
	@Column(name = "tax_type")
	private Integer taxType;
	
	@Column(name = "tax_value")
	private Double taxValue;

}
