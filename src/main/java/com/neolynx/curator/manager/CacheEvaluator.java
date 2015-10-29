package com.neolynx.curator.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.client.InventoryInfo;
import com.neolynx.curator.cache.model.CacheDetail;
import com.neolynx.curator.util.Constants;

/**
 * Created by nitesh.garg on Oct 3, 2015
 */
public class CacheEvaluator {

	static Logger LOGGER = LoggerFactory.getLogger(CacheEvaluator.class);

	private final LoadingCache<String, InventoryInfo> recentItemsCache;
	private final LoadingCache<String, InventoryInfo> differentialInventoryCache;

	public CacheEvaluator(LoadingCache<String, InventoryInfo> differentialInventoryCache,
			LoadingCache<String, InventoryInfo> recentItemsCache) {
		super();
		this.differentialInventoryCache = differentialInventoryCache;
		this.recentItemsCache = recentItemsCache;
	}

	// Simply pull the data from the cache for given vendor
	public List<CacheDetail> getVendorCacheDetails(Long vendorId) {

		List<CacheDetail> cacheResponse = new ArrayList<CacheDetail>();
		LOGGER.trace("Request received for cache details of vendor [{}]", vendorId);

		if (vendorId == null) {
			LOGGER.debug("Invalid request received for missing vendor [{}].", vendorId);
		} else {

			int versionEntriesCount = 0;
			ConcurrentMap<String, InventoryInfo> cacheMap = this.differentialInventoryCache.asMap();
			Set<String> keySet = cacheMap.keySet();

			for (String key : keySet) {

				if (key.startsWith(vendorId + Constants.CACHE_KEY_SEPARATOR_STRING)) {

					CacheDetail cDetail = new CacheDetail();
					cDetail.setVersionId(Long.parseLong(key.substring(key.indexOf(Constants.CACHE_KEY_SEPARATOR_STRING) + 1)));
					cDetail.setResponse(this.differentialInventoryCache.getIfPresent(key));
					cacheResponse.add(cDetail);
					versionEntriesCount++;

				}

			}
			LOGGER.debug("Returning [{}] version cache entries for vendor [{}]", versionEntriesCount, vendorId);
		}

		return cacheResponse;
	}

	// Simply pull the data from the cache for given vendor
	public List<CacheDetail> getRecentItemsCacheDetails() {

		List<CacheDetail> cacheResponse = new ArrayList<CacheDetail>();

		int versionEntriesCount = 0;
		ConcurrentMap<String, InventoryInfo> cacheMap = this.recentItemsCache.asMap();
		Set<String> keySet = cacheMap.keySet();

		for (String key : keySet) {

			CacheDetail cDetail = new CacheDetail();
			cDetail.setResponse(this.recentItemsCache.getIfPresent(key));
			cDetail.setVersionId(cDetail.getResponse() == null ? null : cDetail.getResponse().getNewDataVersionId());
			cacheResponse.add(cDetail);
			versionEntriesCount++;

		}
		LOGGER.debug("Returning [{}] entries for items from recent-items cache.", versionEntriesCount);

		return cacheResponse;
	}

}
