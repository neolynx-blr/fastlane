package com.neolynks.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by nitesh.garg on 05-Sep-2015
 */
@Data
@Entity
@Table(name = "vendor_version_differential")
@NamedQueries({
		@NamedQuery(name = "com.neolynks.model.VendorVersionDifferential.findByVendor", query = "SELECT p FROM VendorVersionDifferential p where vendorId = :vendorId "),
		@NamedQuery(name = "com.neolynks.model.VendorVersionDifferential.findByVendorVersion", query = "SELECT p FROM VendorVersionDifferential p where vendorId = :vendorId and version_id = :versionId"),
		@NamedQuery(name = "com.neolynks.model.VendorVersionDifferential.findAll", query = "SELECT p FROM VendorVersionDifferential p") })
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
	
	@Column(name="is_this_latest_version")
	private Boolean isThisLatestVersion;

	@Column(name = "delta_item_codes")
	private String deltaItemCodes;

	@Column(name="is_valid")
	private Boolean isValid = Boolean.TRUE;
	
	@Column(name = "differential_data")
	private String differentialData;

	@Column(name = "price_differential_data")
	private String priceDifferentialData;

	@Column(name = "last_modified_on", nullable = false)
	private Date lastModifiedOn;
}
