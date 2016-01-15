package com.neolynks.api.common.inventory;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by nitesh.garg on Oct 28, 2015
 *
 */

@Data
public class ProductInfo implements Serializable {

	private static final long serialVersionUID = 1395028613058948189L;
    private String barcode;
	private String name;
	private String tagLine;
	private String imageJSON;
	private String brandName;
}
