package com.example.helloworld.core;

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
@Table(name = "item_detail")
@NamedQueries({ @NamedQuery(name = "com.example.helloworld.core.ItemDetail.findAll", query = "SELECT p FROM ItemDetail p") })
public class ItemDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "item_id", nullable = false)
	private String itemId;

	@Column(name = "version_id", nullable = false)
	private Long versionId;

	@Column(name = "vendor_id", nullable = false)
	private int vendorId;

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
}
