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
 * Created by nitesh.garg on 27-Aug-2015
 */

@Data
@Entity
@Table(name = "all_inventory")
@NamedQueries({ @NamedQuery(name = "com.example.helloworld.core.AllInventory.findAll", query = "SELECT p FROM AllInventory p") })
public class AllInventory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "vendor_id", nullable = false)
	private int vendorId;
	
	@Column(name = "version_id", nullable = false)
	private Long versionId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "tag_line")
	private String tagLine;

	@Column(name = "barcode", nullable = false)
	private Long barcode;
	
	@Column(name = "mrp", nullable = false)
	private Double mrp;

	@Column(name = "price")
	private Double price;

	@Column(name = "image_json")
	private String imageJSON;

	@Column(name = "discount_type")
	private int discountType;

	@Column(name = "discount_value")
	private Double discountValue;
	
	@Column(name = "last_modified_on", nullable = false)
	private Date lastModifiedOn;

}
