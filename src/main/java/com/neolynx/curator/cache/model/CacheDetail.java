package com.neolynx.curator.cache.model;

import com.neolynx.common.model.client.InventoryInfo;

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
