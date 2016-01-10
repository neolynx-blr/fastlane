package com.neolynks.curator.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.neolynks.common.model.cart.CartInfo;
import com.neolynks.common.model.order.ItemRequest;

/**
 * Created by nitesh.garg on Dec 27, 2015
 *
 */

@Data
@EqualsAndHashCode(callSuper=false)
public class Cart implements Serializable {

	private static final long serialVersionUID = -5941199729342263523L;

	private CartInfo base = new CartInfo();
	
	private Long latestVendorDataVersionId;
	private Long onDeviceVendorDataVersionId;

	private Map<Long, ItemRequest> itemList = new HashMap<Long, ItemRequest>();

	private Double netAmount = 0.0D;
	
	private Double discountAmount = 0.0D;

	private Double taxAmount = 0.0D;
	private Double taxableAmount = 0.0D;

	/**
	 * Captures the interactions with Admin service
	 */
	private Boolean cartSyncedWithAdmin = Boolean.FALSE;
	private Map<Long, Integer> adminSyncedBarcodeCount = new HashMap<Long, Integer>();
	
}