/**
 * 
 */
package com.neolynx.curator.manager;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.InventoryResponse;
import com.neolynx.common.model.ItemResponse;
import com.neolynx.common.model.order.ItemDetail;
import com.neolynx.curator.util.Constants;

/**
 * Created by nitesh.garg on Oct 25, 2015
 *
 */
public class PriceEvaluator {
	
	final LoadingCache<Long, Long> vendorVersionCache;
	final LoadingCache<String, InventoryResponse> differentialInventoryCache;

	/**
	 * @param vendorVersionCache
	 * @param differentialInventoryCache
	 */
	public PriceEvaluator(LoadingCache<Long, Long> vendorVersionCache,
			LoadingCache<String, InventoryResponse> differentialInventoryCache) {
		super();
		this.vendorVersionCache = vendorVersionCache;
		this.differentialInventoryCache = differentialInventoryCache;
	}

	public List<ItemDetail> updateItemPricingToLatestVersion(Long vendorId, Long olderDataVersionId, List<ItemDetail> itemDetails) {
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
	public List<ItemDetail> updateItemPricingToLaterVersion(Long vendorId, Long olderDataVersionId, Long newDataVersionId, List<ItemDetail> itemDetails) {
		
		/**
		 * TODO For now assuming the new data version is same as the most recent one.
		 */
		
		if(newDataVersionId.compareTo(olderDataVersionId) == 0) {
			return itemDetails;
		}
		
		
		InventoryResponse inventoryResponse = this.differentialInventoryCache.getIfPresent(vendorId + Constants.CACHE_KEY_SEPARATOR_STRING + newDataVersionId);
		
		Map<String, ItemResponse> itemLatestDataMap = new HashedMap<String, ItemResponse>();
		
		/**
		 * TODO This should probably be added to cache as well.
		 */
		for(ItemResponse itemResponse : inventoryResponse.getItemsUpdated()) {
			itemLatestDataMap.put(itemResponse.getItemCode(), itemResponse);
		}
		
		for(ItemDetail instance : itemDetails) {
			
			/**
			 * TODO Check if something has changed, if so update the pricing and
			 * mark the boolean flag accordingly. Need special attention for
			 * evaluating the discounts here.
			 */
			instance.getCount();
			
		}
		
		return itemDetails;
		
	}

}
