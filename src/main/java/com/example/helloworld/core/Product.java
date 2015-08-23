package com.example.helloworld.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "product")
@NamedQueries({ @NamedQuery(name = "com.example.helloworld.core.Product.findAll", query = "SELECT p FROM Product p") })
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "barcode", nullable = false)
	private String barcode;

	@Column(name = "tagLine")
	private String tagLine;

	@Column(name = "description")
	private String description;

	@Column(name = "imageURLJSON", nullable = false)
	private String imageURLJSON;

	public Product() {
	}

	public Product(long id, String name, String barcode, String tagLine,
			String description, String imageURLJSON) {
		super();
		this.id = id;
		this.name = name;
		this.barcode = barcode;
		this.tagLine = tagLine;
		this.description = description;
		this.imageURLJSON = imageURLJSON;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getTagLine() {
		return tagLine;
	}

	public void setTagLine(String tagLine) {
		this.tagLine = tagLine;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageURLJSON() {
		return imageURLJSON;
	}

	public void setImageURLJSON(String imageURLJSON) {
		this.imageURLJSON = imageURLJSON;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((barcode == null) ? 0 : barcode.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result
				+ ((imageURLJSON == null) ? 0 : imageURLJSON.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tagLine == null) ? 0 : tagLine.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Product other = (Product) obj;
		if (barcode == null) {
			if (other.barcode != null)
				return false;
		} else if (!barcode.equals(other.barcode))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (imageURLJSON == null) {
			if (other.imageURLJSON != null)
				return false;
		} else if (!imageURLJSON.equals(other.imageURLJSON))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (tagLine == null) {
			if (other.tagLine != null)
				return false;
		} else if (!tagLine.equals(other.tagLine))
			return false;
		return true;
	}

}
