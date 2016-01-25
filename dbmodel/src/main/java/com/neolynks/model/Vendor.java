package com.neolynks.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by nitesh.garg on 25-Aug-2015
 */

@Data
@Entity
@Table(name = "vendor")
@NamedQueries({ @NamedQuery(name = "com.neolynks.model.Vendor.findAll", query = "SELECT p FROM Vendor p") })
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
