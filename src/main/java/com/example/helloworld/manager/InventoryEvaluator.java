package com.example.helloworld.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.helloworld.util.Constants;
import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.CacheDetail;
import com.neolynx.common.model.InventoryResponse;

public class InventoryEvaluator {

	private final LoadingCache<Long, Long> vendorVersionCache;
	static Logger LOGGER = LoggerFactory.getLogger(InventoryEvaluator.class);
	private final LoadingCache<String, InventoryResponse> differentialInventoryCache;

	public InventoryEvaluator(LoadingCache<String, InventoryResponse> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache) {
		super();
		this.differentialInventoryCache = differentialInventoryCache;
		this.vendorVersionCache = vendorVersionCache;
	}

	// Simply pull the data from the cache for given vendor
	public List<CacheDetail> getCacheDetails(Long vendorId) {

		List<CacheDetail> cacheResponse = new ArrayList<CacheDetail>();
		LOGGER.debug("Request received for cache details of vendor [{}]", vendorId);

		if (vendorId == null) {
			LOGGER.debug("Invalid request received for missing vendor.");
		} else {

			ConcurrentMap<String, InventoryResponse> cacheMap = this.differentialInventoryCache.asMap();
			Set<String> keySet = cacheMap.keySet();
			
			for (String key : keySet) {

				if (key.startsWith(vendorId + Constants.VENDOR_VERSION_KEY_SEPARATOR)) {

					CacheDetail cDetail = new CacheDetail();
					cDetail.setVersionId(Long.parseLong(key.substring(key.indexOf(Constants.VENDOR_VERSION_KEY_SEPARATOR) + 1)));
					cDetail.setResponse(this.differentialInventoryCache.getIfPresent(key));
					cacheResponse.add(cDetail);

				}

			}

		}

		return cacheResponse;
	}
	
	// Simply pull the data from the cache
	public InventoryResponse getInventoryDifferential(Long vendorId, Long dataVersionId) {

		InventoryResponse inventoryResponse = null;
		LOGGER.debug("Request received for inventory differential for vendor-version [{}-{}]", vendorId, dataVersionId);

		if (vendorId == null || dataVersionId == null) {
			LOGGER.debug("Invalid request received for missing vendor and/or version id.");

			inventoryResponse = new InventoryResponse();
			inventoryResponse.setIsError(Boolean.TRUE);
			inventoryResponse.setVendorId(vendorId);
			inventoryResponse.setCurrentDataVersionId(dataVersionId);

		} else {

			inventoryResponse = this.differentialInventoryCache.getIfPresent(vendorId + "-" + dataVersionId);
			if (inventoryResponse == null) {
				LOGGER.debug(
						"Unable to get anydata from the cache for vendor-version [{}-{}], will instead pull latest inventory.",
						vendorId, dataVersionId);
				inventoryResponse = getLatestInventory(vendorId);
			}
		}

		return inventoryResponse;
	}

	public InventoryResponse getLatestInventory(Long vendorId) {

		InventoryResponse inventoryResponse = null;

		if (vendorId == null) {
			LOGGER.debug("Invalid request received for NULL vendor id.");
			inventoryResponse = new InventoryResponse();
			inventoryResponse.setIsError(Boolean.TRUE);
			inventoryResponse.setVendorId(vendorId);

		} else {
			Long latestVersionId = this.vendorVersionCache.getIfPresent(vendorId);
			LOGGER.debug("The latest version found for vendor [{}] is [{}], looking for differential data now.",
					vendorId, latestVersionId);
			inventoryResponse = this.differentialInventoryCache.getIfPresent(vendorId
					+ Constants.VENDOR_VERSION_KEY_SEPARATOR + latestVersionId);
		}

		return inventoryResponse;
	}

}
