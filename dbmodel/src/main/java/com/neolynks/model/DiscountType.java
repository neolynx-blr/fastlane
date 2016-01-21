package com.neolynks.model;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by nitesh.garg on 25-Aug-2015
 */

@Data
@Entity
@Table(name = "inventory.discount_type")
@NamedQueries({ @NamedQuery(name = "com.neolynks.model.DiscountType.findAll", query = "SELECT p FROM DiscountType p") })
public class DiscountType {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

}
