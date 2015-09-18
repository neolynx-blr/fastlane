package com.neolynx.common.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by nitesh.garg on 26-Aug-2015
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class InventoryResponse extends ResponseAudit {

	private static final long serialVersionUID = -3415988252429606589L;

	private Long vendorId;
	
	private Long newDataVersionId;
	private Long currentDataVersionId;
	
	private List<ItemResponse> itemsAdded;
	private List<ItemResponse> itemsUpdated;
	private List<ItemResponse> itemsRemoved;
	
}
