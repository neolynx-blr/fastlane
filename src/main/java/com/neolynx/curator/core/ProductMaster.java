package com.neolynx.curator.core;

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
@Table(name = "product_master")
@NamedQueries({
		@NamedQuery(name = "com.example.helloworld.core.ProductMaster.findAll", query = "SELECT p FROM ProductMaster p"),
		@NamedQuery(name = "com.example.helloworld.core.ProductMaster.findExclusiveToVendor", query = "SELECT p FROM ProductMaster p where vendor_id = :vendorId "),
		@NamedQuery(name = "com.example.helloworld.core.ProductMaster.findByVendor", query = "SELECT p FROM ProductMaster p where vendor_id ilike :vendorIdPattern "),
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
		if (this.getName().trim().equalsIgnoreCase(o.getName().trim())
				&& this.getDescription().trim().equalsIgnoreCase(o.getDescription().trim())
				&& this.getTagLine().trim().equalsIgnoreCase(o.getTagLine().trim())
				&& this.getBarcode().compareTo(o.getBarcode()) == 0) {
			return 0;
		} else {
			
			/*
			System.out.println("Something didn't match");

			if (this.getBarcode().compareTo(o.getBarcode()) == 0) {
				System.out.println("Barcode matched");
			} else {
				System.out.println("Barcode didn't match" + this.getBarcode() + "&" + o.getBarcode());
			}

			if (this.getName().trim().equalsIgnoreCase(o.getName().trim())) {
				System.out.println("Name matched");
			} else {
				System.out.println("Name didn't match" + this.getName() + "&" + o.getName());
			}

			if (this.getDescription().trim().equalsIgnoreCase(o.getDescription().trim())) {
				System.out.println("getDescription matched");
			} else {
				System.out.println("getDescription didn't match" + this.getDescription() + "&" + o.getDescription());
			}

			if (this.getTagLine().trim().equalsIgnoreCase(o.getTagLine().trim())) {
				System.out.println("getTagLine matched");
			} else {
				System.out.println("getTagLine didn't match" + this.getTagLine() + "&" + o.getTagLine());
			}
			*/
}

		return 1;
	}

}
