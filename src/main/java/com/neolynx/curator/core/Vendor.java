package com.neolynx.curator.core;

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
@Table(name = "vendor")
@NamedQueries({ @NamedQuery(name = "com.example.helloworld.core.Vendor.findAll", query = "SELECT p FROM Vendor p") })
public class Vendor {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "address", nullable = false)
	private String address;
	
	@Column(name = "created_on", nullable = false)
	private Date createdOn;
	
	@Column(name = "last_inventory_update_on", nullable = false)
	private Date lastInventoryUpdateOn;
		
	@Column(name = "last_updated_on", nullable = false)
	private Date lastUpdatedOn;		
}
