package com.neolynks.curator.cache.inventory.model;


import com.neolynks.api.common.inventory.InventoryInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by nitesh.garg on 21-Sep-2015
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class CacheDetail {

	private Long versionId;
	private InventoryInfo response;
}
