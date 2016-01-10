package com.neolynks.vendor.model;

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
	
	private String vendorUserName;
	private String vendorPassword;

	private String inventoryMasterFileName;

	public String getStatusFileNamePrint() {
		return this.statusFileName.substring(this.statusFileName.lastIndexOf("/") + 1);
	}

	public String getInventoryFileNamePrint() {
		return this.inventoryFileName.substring(this.inventoryFileName.lastIndexOf("/") + 1);
	}

	public String getLastSyncIdFileNamePrint() {
		return this.lastSyncIdFileName.substring(this.lastSyncIdFileName.lastIndexOf("/") + 1);
	}

	public String getBackupFileNameForInventoryPrint() {
		return this.backupFileNameForInventory.substring(this.backupFileNameForInventory.lastIndexOf("/") + 1);
	}

	public String getInventoryMasterFileNamePrint() {
		return this.inventoryMasterFileName.substring(this.inventoryMasterFileName.lastIndexOf("/") + 1);
	}

	private int lastSyncIdType;

}
