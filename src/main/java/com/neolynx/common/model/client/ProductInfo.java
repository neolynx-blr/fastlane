/**
 * 
 */
package com.neolynx.common.model.client;

import java.io.Serializable;

import lombok.Data;

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

}
