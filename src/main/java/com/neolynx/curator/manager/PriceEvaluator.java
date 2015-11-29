/**
 * 
 */
package com.neolynx.curator.manager;

import java.util.List;

import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.client.InventoryInfo;
import com.neolynx.common.model.order.ItemRequest;

/**
 * Created by nitesh.garg on Oct 25, 2015
 *
 */
public class PriceEvaluator {
	
	final LoadingCache<Long, Long> vendorVersionCache;
	final LoadingCache<String, InventoryInfo> differentialInventoryCache;

	/**
	 * @param vendorVersionCache
	 * @param differentialInventoryCache
	 */
	public PriceEvaluator(LoadingCache<Long, Long> vendorVersionCache,
			LoadingCache<String, InventoryInfo> differentialInventoryCache) {
		super();
		this.vendorVersionCache = vendorVersionCache;
		this.differentialInventoryCache = differentialInventoryCache;
	}

	public List<ItemRequest> updateItemPricingToLatestVersion(Long vendorId, Long olderDataVersionId, List<ItemRequest> itemDetails) {
		Long latestVersionId = this.vendorVersionCache.getIfPresent(vendorId);
		return updateItemPricingToLaterVersion(vendorId, olderDataVersionId, latestVersionId, itemDetails);
	}
		
	/**
	 * This call needs to return updated item-details, specially in terms of
	 * pricing and discounts, if something has changed in the latest data
	 * version. Since the caller needs to know if anything has changed, the list
	 * will be returned null/empty to indicate the same. This can be done in a
	 * better way later on. Hence, TODO Possibly change the way inventory
	 * differential cache is being stored.
	 * 
	 * @param vendorId
	 * @param olderDataVersionId
	 * @param newDataVersionId
	 * @param itemDetails
	 * @return
	 */
	public List<ItemRequest> updateItemPricingToLaterVersion(Long vendorId, Long olderDataVersionId, Long newDataVersionId, List<ItemRequest> itemDetails) {
		
		/**
		 * TODO For now assuming the new data version is same as the most recent one.
		 */
		
		if(newDataVersionId.compareTo(olderDataVersionId) == 0) {
			return itemDetails;
		}
		
		
		//InventoryInfo inventoryResponse = this.differentialInventoryCache.getIfPresent(vendorId + Constants.CACHE_KEY_SEPARATOR_STRING + newDataVersionId);
		
		//Map<String, ItemInfo> itemLatestDataMap = new HashedMap<String, ItemInfo>();
		
		/**
		 * TODO This should probably be added to cache as well.
		 *
		for(ItemInfo itemResponse : inventoryResponse.getItemsUpdated()) {
			itemLatestDataMap.put(itemResponse.getItemCode(), itemResponse);
		}*/
		
		for(ItemRequest instance : itemDetails) {
			
			/**
			 * TODO Check if something has changed, if so update the pricing and
			 * mark the boolean flag accordingly. Need special attention for
			 * evaluating the discounts here.
			 */
			instance.getCountForInStorePickup();
			
		}
		
		return itemDetails;
		
	}

}
