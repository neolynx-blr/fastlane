package com.neolynks.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by nitesh.garg on 27-Aug-2015
 */

@Data
@Entity
@Table(name = "inventory_master")
@NamedQueries({
		@NamedQuery(name = "com.neolynks.model.InventoryMaster.findInventoryByUniqueConstraint", query = "SELECT p FROM InventoryMaster p where p.vendorId = :vendorId and p.versionId = :versionId and (p.barcode = :barcode or p.itemCode = :itemCode)"),
		@NamedQuery(name = "com.neolynks.model.AllInventory.findRecentInventoryUpdates", query = "select p from InventoryMaster p "
				+ " where vendorId = :vendorId "
				+ " and versionId = ("
				+ "	select max(versionId) from InventoryMaster "
				+ " where barcode = p.barcode "
				+ " and vendorId = :vendorId " + " and versionId >= :lastSyncedVersionId )  ") })
public class InventoryMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "barcode", nullable = false)
	private String barcode;
	@Column(name = "item_code", nullable = false)
	private String itemCode;
	
	@Column(name = "tag_line")
	private String tagLine;
	@Column(name = "benefits")
	private String benefits;	
	@Column(name = "brand_name")
	private String brandName;
	@Column(name = "image_json")
	private String imageJSON;	
	@Column(name = "how_to_use")
	private String howToUse;
	@Column(name = "description")
	private String description;

	@Column(name = "category_id")
	private String categoryId;
	@Column(name = "category_text")
	private String categoryText;
	
	@Column(name = "info_json")
	private String infoJSON;

	@Column(name = "vendor_id", nullable = false)
	private Long vendorId;
	@Column(name = "version_id", nullable = false)
	private Long versionId;
	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "mrp", nullable = false)
	private Double mrp;
	@Column(name = "selling_price")
	private Double sellingPrice;
	@Column(name = "base_price")
	private Double basePrice;
	
	@Column(name = "tax_json")
	private String taxJSON;
	@Column(name = "discount_json")
	private String discountJSON;
	
	@Column(name = "created_on")
	private Date createdOn;

}
