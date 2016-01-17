package com.neolynks.curator.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.neolynks.api.common.Response;
import com.neolynks.api.common.inventory.InventoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.LoadingCache;
import com.neolynks.curator.cache.model.CacheDetail;
import com.neolynks.curator.util.Constants;

/**
 * Created by nitesh.garg on Oct 3, 2015
 */
public class CacheEvaluator {

	static Logger LOGGER = LoggerFactory.getLogger(CacheEvaluator.class);

	private final LoadingCache<String, InventoryInfo> recentItemsCache;
	private final LoadingCache<Long, String> currentInventoryCache;
	private final LoadingCache<String, InventoryInfo> differentialInventoryCache;

	public CacheEvaluator(LoadingCache<String, InventoryInfo> differentialInventoryCache,
			LoadingCache<String, InventoryInfo> recentItemsCache, LoadingCache<Long, String> currentInventoryCache) {
		super();
		this.differentialInventoryCache = differentialInventoryCache;
		this.recentItemsCache = recentItemsCache;
		this.currentInventoryCache = currentInventoryCache;
	}

	// Simply pull the data from the cache for given vendor
	public Response<List<CacheDetail>> getVendorCacheDetails(Long vendorId) {
        Response<List<CacheDetail>> cacheResponse = new Response<>();
		List<CacheDetail> cacheDetails = new ArrayList<CacheDetail>();
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
                    cacheDetails.add(cDetail);
					versionEntriesCount++;

				}

			}
			LOGGER.debug("Returning [{}] version cache entries for vendor [{}]", versionEntriesCount, vendorId);
		}
        cacheResponse.setIsError(false);
		return cacheResponse;
	}

	// Simply pull the data from the cache for given vendor
	public Response<List<CacheDetail>> getCurrentInventoryCacheDetails(Long vendorId) {
        Response<List<CacheDetail>> cacheResponse = new Response<>();
        List<CacheDetail> cacheDetails = new ArrayList<CacheDetail>();

		ObjectMapper mapper = new ObjectMapper();

		int versionEntriesCount = 0;
		CacheDetail cDetail = new CacheDetail();
		String inventoryCacheAsString = this.currentInventoryCache.getIfPresent(vendorId);
		try {
			cDetail.setResponse(mapper.readValue(inventoryCacheAsString, InventoryInfo.class));
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Error [{}] while deserializing the read current inventory from the cache", e.getMessage());
		}
		cDetail.setVersionId(cDetail.getResponse() == null ? null : cDetail.getResponse().getNewDataVersionId());
        cacheDetails.add(cDetail);
		versionEntriesCount++;
		LOGGER.debug("Returning [{}] entries for items from recent-items cache.", versionEntriesCount);

        cacheResponse.setIsError(false);
        return cacheResponse;
    }

	// Simply pull the data from the cache for given vendor
	public Response<List<CacheDetail>> getRecentItemsCacheDetails() {
        Response<List<CacheDetail>> cacheResponse = new Response<>();
		List<CacheDetail> cacheDetails = new ArrayList<CacheDetail>();

		int versionEntriesCount = 0;
		ConcurrentMap<String, InventoryInfo> cacheMap = this.recentItemsCache.asMap();
		Set<String> keySet = cacheMap.keySet();

		for (String key : keySet) {

			CacheDetail cDetail = new CacheDetail();
			cDetail.setResponse(this.recentItemsCache.getIfPresent(key));
			cDetail.setVersionId(cDetail.getResponse() == null ? null : cDetail.getResponse().getNewDataVersionId());
            cacheDetails.add(cDetail);
			versionEntriesCount++;

		}
		LOGGER.debug("Returning [{}] entries for items from recent-items cache.", versionEntriesCount);
        cacheResponse.setIsError(false);
		return cacheResponse;
	}

}
