package com.neolynx.common.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * Created by nitesh.garg on 15-Sep-2015
 */

@Data
public class InventoryRequest implements Serializable {
	
	private static final long serialVersionUID = 4922735532337655734L;
	
	private Long vendorId;
	private Long dataVersionId;
	
	private List<ItemMaster> itemsUpdated;
	
}
