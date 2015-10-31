package com.neolynx.common.model.client;

import java.io.Serializable;

import lombok.Data;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by nitesh.garg on Oct 28, 2015
 *
 */

@Data
public class ProductInfo implements Serializable {

	private static final long serialVersionUID = 1395028613058948189L;

	private String name;
	private Boolean isName = Boolean.FALSE;

	private String tagLine;
	private Boolean isTagLine = Boolean.FALSE;

	private String imageJSON;
	private Boolean isImageJSON = Boolean.FALSE;

	private String description;
	private Boolean isDescription = Boolean.FALSE;

	private String benefits;
	private Boolean isBenefits = Boolean.FALSE;

	private String howToUse;
	private Boolean isHowToUse = Boolean.FALSE;

	private String brandName;
	private Boolean isBrandName = Boolean.FALSE;

	public ProductInfo generateDifferentialFrom(ProductInfo newProductInfo) {

		if (newProductInfo != null) {

			ProductInfo differentialProductInfo = new ProductInfo();

			if (this.getName() == null || !this.getName().equals(newProductInfo.getName())) {
				differentialProductInfo.setName(newProductInfo.getName());
			}

			if (this.getTagLine() == null || !this.getTagLine().equals(newProductInfo.getTagLine())) {
				differentialProductInfo.setTagLine(newProductInfo.getTagLine());
			}

			if (this.getImageJSON() == null || !this.getImageJSON().equals(newProductInfo.getImageJSON())) {
				differentialProductInfo.setImageJSON(newProductInfo.getImageJSON());
			}

			if (this.getDescription() == null || !this.getDescription().equals(newProductInfo.getDescription())) {
				differentialProductInfo.setDescription(newProductInfo.getDescription());
			}

			if (this.getBenefits() == null || !this.getBenefits().equals(newProductInfo.getBenefits())) {
				differentialProductInfo.setBenefits(newProductInfo.getBenefits());
			}

			if (this.getHowToUse() == null || !this.getHowToUse().equals(newProductInfo.getHowToUse())) {
				differentialProductInfo.setHowToUse(newProductInfo.getHowToUse());
			}

			if (this.getBrandName() == null || !this.getBrandName().equals(newProductInfo.getBrandName())) {
				differentialProductInfo.setBrandName(newProductInfo.getBrandName());
			}

			return differentialProductInfo;
		}

		return null;
	}

	public void setName(String name) {
		if (!StringUtils.equals(name, this.name)) {
			this.name = name;
			this.setIsName(Boolean.TRUE);
		}
	}

	public void setTagLine(String tagLine) {
		if (!StringUtils.equals(tagLine, this.tagLine)) {
			this.tagLine = tagLine;
			this.setIsTagLine(Boolean.TRUE);
		}
	}

	public void setImageJSON(String imageJSON) {
		if (!StringUtils.equals(imageJSON, this.imageJSON)) {
			this.imageJSON = imageJSON;
			this.setIsImageJSON(Boolean.TRUE);
		}
	}

	public void setDescription(String description) {
		if (!StringUtils.equals(description, this.description)) {
			this.description = description;
			this.setIsDescription(Boolean.TRUE);
		}
	}

	public void setBenefits(String benefits) {
		if (!StringUtils.equals(benefits, this.benefits)) {
			this.benefits = benefits;
			this.setIsBenefits(Boolean.TRUE);
		}
	}

	public void setHowToUse(String howToUse) {
		if (!StringUtils.equals(howToUse, this.howToUse)) {
			this.howToUse = howToUse;
			this.setIsHowToUse(Boolean.TRUE);
		}
	}

	public void setBrandName(String brandName) {
		if (!StringUtils.equals(brandName, this.brandName)) {
			this.brandName = brandName;
			this.setIsBrandName(Boolean.TRUE);
		}
	}

	public void reset() {
		this.setIsName(Boolean.FALSE);
		this.setIsTagLine(Boolean.FALSE);
		this.setIsImageJSON(Boolean.FALSE);
		this.setIsDescription(Boolean.FALSE);
		this.setIsBenefits(Boolean.FALSE);
		this.setIsHowToUse(Boolean.FALSE);
		this.setIsBrandName(Boolean.FALSE);
	}

}
