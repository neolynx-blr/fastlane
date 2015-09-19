package com.neolynx.curator.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by nitesh.garg on 17-Sep-2015
 */

@Data
public class CurationConfig implements Serializable {

	private static final long serialVersionUID = 420048540249500055L;

	private Long vendorId;
	private Integer maxRowCountForServerPost;

	private String statusFileName;
	private String inventoryFileName;
	private String lastSyncIdFileName;
	private String backupFileNameForInventory;
	
	private int lastSyncIdType;

}
