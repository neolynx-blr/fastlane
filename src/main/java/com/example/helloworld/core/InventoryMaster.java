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
 * Created by nitesh.garg on 27-Aug-2015
 */

@Data
@Entity
@Table(name = "inventory_master")
@NamedQueries({
		@NamedQuery(name = "com.example.helloworld.core.InventoryMaster.findInventoryByUniqueConstraint", query = "SELECT p FROM InventoryMaster p where p.vendorId = :vendorId and p.versionId = :versionId and (p.barcode = :barcode or p.itemCode = :itemCode)"),
		@NamedQuery(name = "com.example.helloworld.core.AllInventory.findRecentInventoryUpdates", query = "select p from InventoryMaster p "
				+ " where vendorId = :vendorId "
				+ " and versionId = ("
				+ "	select max(versionId) from InventoryMaster "
				+ " where barcode = p.barcode "
				+ " and vendorId = :vendorId " + " and versionId >= :lastSyncedVersionId )  ") })
public class InventoryMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "vendor_id", nullable = false)
	private Long vendorId;

	@Column(name = "item_code", nullable = false)
	private String itemCode;

	@Column(name = "version_id", nullable = false)
	private Long versionId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "tag_line")
	private String tagLine;

	@Column(name = "barcode", nullable = false)
	private Long barcode;

	@Column(name = "mrp", nullable = false)
	private Double mrp;

	@Column(name = "price")
	private Double price;

	@Column(name = "image_json")
	private String imageJSON;

	@Column(name = "discount_type")
	private Integer discountType;

	@Column(name = "discount_value")
	private Double discountValue;

	@Column(name = "created_on", nullable = false)
	private Date createdOn;

}
