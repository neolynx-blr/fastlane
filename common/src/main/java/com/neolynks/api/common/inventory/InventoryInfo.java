package com.neolynks.api.common.inventory;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by nitesh.garg on 26-Aug-2015
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class InventoryInfo {

	private static final long serialVersionUID = -3415988252429606589L;

	private Long vendorId;

	private Long newDataVersionId;
	private Long currentDataVersionId;

	private Set<Long> deletedItems = new HashSet<Long>();
	private Map<Long, ItemInfo> addedItems = new HashMap<Long, ItemInfo>();
	private Map<Long, ItemInfo> updatedItems = new HashMap<Long, ItemInfo>();

}
