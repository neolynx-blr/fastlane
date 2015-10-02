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
 * Created by nitesh.garg on 29-Aug-2015
 */

@Data
@Entity
@Table(name = "inventory.inventory_sync_master")
@NamedQueries({ @NamedQuery(name = "com.example.helloworld.core.InventorySyncStatus.findAll", query = "SELECT p FROM InventorySyncStatus p") })
public class InventorySyncStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "vendor_id", nullable = false)
	private Long vendorId;

	@Column(name = "last_synced_version_id", nullable = false)
	private Long lastSyncedVersionId;

	@Column(name = "last_modified_on", nullable = false)
	private Date lastModifiedOn;

}
