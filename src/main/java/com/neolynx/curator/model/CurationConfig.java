package com.neolynx.curator.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by nitesh.garg on 17-Sep-2015
 */

@Data
public class CurationConfig implements Serializable {

	private static final long serialVersionUID = 420048540249500055L;

	public Long vendorId;
	public Integer maxRowCountForServerPost;

	public String statusFileName;
	public String inventoryFileName;

}
