package com.neolynks.curator.manager;

import com.neolynks.api.common.inventory.InventoryInfo;
import com.neolynks.api.common.inventory.ItemInfo;

import com.google.common.cache.LoadingCache;
import com.neolynks.curator.db.OrderDetailDAO;

/**
 * Created by nitesh.garg on Oct 22, 2015
 *
 */
public class OrderProcessor {

	final OrderDetailDAO orderDetailDAO;
	final LoadingCache<Long, Long> vendorVersionCache;
	final LoadingCache<String, ItemInfo> vendorBarcodeInventoryCache;
	final LoadingCache<String, InventoryInfo> differentialInventoryCache;

	/**
	 * @param orderDetailDAO
	 * @param vendorBarcodeInventoryCache
	 */
	public OrderProcessor(OrderDetailDAO orderDetailDAO,
			LoadingCache<Long, Long> vendorVersionCache,
			LoadingCache<String, InventoryInfo> differentialInventoryCache,
			LoadingCache<String, ItemInfo> vendorBarcodeInventoryCache) {
		super();
		this.orderDetailDAO = orderDetailDAO;
        this.vendorVersionCache = vendorVersionCache;
		this.differentialInventoryCache = differentialInventoryCache;
		this.vendorBarcodeInventoryCache = vendorBarcodeInventoryCache;
	}

}
