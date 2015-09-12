package com.example.helloworld.core;

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

@Data()
@Entity
@Table(name = "product_master")
@NamedQueries({
		@NamedQuery(name = "com.example.helloworld.core.ProductMaster.findAll", query = "SELECT p FROM ProductMaster p"),
		@NamedQuery(name = "com.example.helloworld.core.ProductMaster.fetchByBarcode", query = "SELECT p FROM ProductMaster p where barcode = :barcode") })
public class ProductMaster implements Comparable<ProductMaster> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "vendor_id", nullable = false)
	private String vendorId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "image_json")
	private String imageJSON;

	@Column(name = "tag_line")
	private String tagLine;

	@Column(name = "barcode", nullable = false)
	private Long barcode;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ProductMaster o) {

		/*
		 * TODO Not looking at Image-JSON yet
		 */
		if (this.getName().equalsIgnoreCase(o.getName()) && this.getDescription().equalsIgnoreCase(o.getDescription())
				&& this.getTagLine().equalsIgnoreCase(o.getTagLine()) && this.getBarcode() == o.getBarcode()) {
			return 0;
		}

		return 1;
	}

}
