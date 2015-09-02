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
@Table(name = "product_core")
@NamedQueries(
		{ 
			@NamedQuery(name = "com.example.helloworld.core.ProductCore.findAll", query = "SELECT p FROM ProductCore p"),
			@NamedQuery(name = "com.example.helloworld.core.ProductCore.fetchByBarcode", query = "SELECT p FROM ProductCore p where barcode = :barcode")
		})
public class ProductCore {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "vendor_id", nullable = false)
	private Long vendorId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "tag_line")
	private String tagLine;

	@Column(name = "barcode", nullable = false)
	private Long barcode;
}
