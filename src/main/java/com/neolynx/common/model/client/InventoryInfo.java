package com.neolynx.common.model.client;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.neolynx.common.model.BaseResponse;

/**
 * Created by nitesh.garg on 26-Aug-2015
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class InventoryInfo extends BaseResponse {

	private static final long serialVersionUID = -3415988252429606589L;

	private Long vendorId;
	
	/**
	 * This flag indicated if the data contained in the instance of this object
	 * is focusing on pricing of the inventory contained or else the complete
	 * information of every item
	 */
	private Boolean isDataPriceOriented = Boolean.FALSE;
	
	private Long newDataVersionId;
	private Long currentDataVersionId;
	
	private List<ItemInfo> itemsAdded;
	private List<ItemInfo> itemsUpdated;
	private List<ItemInfo> itemsRemoved;
	
}
