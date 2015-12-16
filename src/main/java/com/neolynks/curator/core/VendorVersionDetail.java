package com.neolynks.curator.core;

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
 * Created by nitesh.garg on 05-Sep-2015
 */
@Data
@Entity
@Table(name = "vendor_version_detail")
@NamedQueries({ @NamedQuery(name = "com.example.helloworld.core.VendorVersionDetail.findByVendor", query = "SELECT p FROM VendorVersionDetail p where vendorId = :vendorId") })
public class VendorVersionDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "vendor_id", nullable = false)
	private Long vendorId;

	@Column(name = "latest_synced_version_id")
	private Long latestSyncedVersionId;

	@Column(name = "valid_version_ids")
	private String validVersionIds;

	@Column(name = "current_inventory")
	private String currentInventory;
	
	@Column(name = "last_modified_on", nullable = false)
	private Date lastModifiedOn;

}
