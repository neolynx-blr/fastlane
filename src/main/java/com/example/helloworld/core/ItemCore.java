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
@Table(name = "item_core")
@NamedQueries({ @NamedQuery(name = "com.example.helloworld.core.ItemCore.findAll", query = "SELECT p FROM ItemCore p") })
public class ItemCore {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "vendor_id", nullable = false)
	private int vendorId;

	@Column(name = "product_id", nullable = false)
	private Long productId;

}
