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
 * Created by nitesh.garg on 29-Aug-2015
 */

@Data
@Entity
@Table(name = "inventory_sync")
@NamedQueries({ @NamedQuery(name = "com.example.helloworld.core.InventorySync.findAll", query = "SELECT p FROM InventorySync p") })
public class InventorySync {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "last_synced_version_id", nullable = false)
	private Long lastSyncedVersionId;

	@Column(name = "vendor_id", nullable = false)
	private Long vendorId;

	@Column(name = "last_modified_on", nullable = false)
	private Date lastModifiedOn;

}
