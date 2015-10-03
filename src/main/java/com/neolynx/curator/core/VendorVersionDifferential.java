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
 * Created by nitesh.garg on 05-Sep-2015
 */
@Data
@Entity
@Table(name = "vendor_version_differential")
@NamedQueries({
		@NamedQuery(name = "com.example.helloworld.core.VendorVersionDifferential.findByVendor", query = "SELECT p FROM VendorVersionDifferential p where vendorId = :vendorId "),
		@NamedQuery(name = "com.example.helloworld.core.VendorVersionDifferential.findByVendorVersion", query = "SELECT p FROM VendorVersionDifferential p where vendorId = :vendorId and version_id = :versionId"),
		@NamedQuery(name = "com.example.helloworld.core.VendorVersionDifferential.findAll", query = "SELECT p FROM VendorVersionDifferential p") })
public class VendorVersionDifferential {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "vendor_id", nullable = false)
	private Long vendorId;

	@Column(name = "version_id")
	private Long versionId;

	@Column(name = "last_synced_version_id")
	private Long lastSyncedVersionId;

	@Column(name = "delta_item_codes")
	private String deltaItemCodes;

	@Column(name = "last_modified_on", nullable = false)
	private Date lastModifiedOn;
}
