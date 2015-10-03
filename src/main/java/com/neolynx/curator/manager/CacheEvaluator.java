package com.neolynx.curator.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.CacheDetail;
import com.neolynx.common.model.InventoryResponse;
import com.neolynx.curator.util.Constants;

/**
 * Created by nitesh.garg on Oct 3, 2015
 */
public class CacheEvaluator {

	static Logger LOGGER = LoggerFactory.getLogger(CacheEvaluator.class);

	private final LoadingCache<String, InventoryResponse> differentialInventoryCache;

	public CacheEvaluator(LoadingCache<String, InventoryResponse> differentialInventoryCache) {
		super();
		this.differentialInventoryCache = differentialInventoryCache;
	}

	// Simply pull the data from the cache for given vendor
	public List<CacheDetail> getCacheDetails(Long vendorId) {

		List<CacheDetail> cacheResponse = new ArrayList<CacheDetail>();
		LOGGER.trace("Request received for cache details of vendor [{}]", vendorId);

		if (vendorId == null) {
			LOGGER.debug("Invalid request received for missing vendor [{}].", vendorId);
		} else {

			int versionEntriesCount = 0;
			ConcurrentMap<String, InventoryResponse> cacheMap = this.differentialInventoryCache.asMap();
			Set<String> keySet = cacheMap.keySet();

			for (String key : keySet) {

				if (key.startsWith(vendorId + Constants.VENDOR_VERSION_KEY_SEPARATOR)) {

					CacheDetail cDetail = new CacheDetail();
					cDetail.setVersionId(Long.parseLong(key.substring(key
							.indexOf(Constants.VENDOR_VERSION_KEY_SEPARATOR) + 1)));
					cDetail.setResponse(this.differentialInventoryCache.getIfPresent(key));
					cacheResponse.add(cDetail);
					versionEntriesCount++;

				}

			}
			LOGGER.debug("Returning [{}] version cache entries for vendor [{}]", versionEntriesCount, vendorId);
		}

		return cacheResponse;
	}

}
