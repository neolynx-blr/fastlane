package com.neolynx.curator.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.InventoryResponse;
import com.neolynx.curator.util.Constants;

public class InventoryEvaluator {

	private final LoadingCache<Long, Long> vendorVersionCache;
	static Logger LOGGER = LoggerFactory.getLogger(InventoryEvaluator.class);
	private final LoadingCache<String, InventoryResponse> differentialInventoryCache;
	private final LoadingCache<String, InventoryResponse> recentItemCache;

	public InventoryEvaluator(LoadingCache<String, InventoryResponse> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache, LoadingCache<String, InventoryResponse> recentItemCache) {
		super();
		this.differentialInventoryCache = differentialInventoryCache;
		this.vendorVersionCache = vendorVersionCache;
		this.recentItemCache = recentItemCache;
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
					+ Constants.CACHE_KEY_SEPARATOR_STRING + latestVersionId);
		}

		return inventoryResponse;
	}

	public InventoryResponse getLatestItemForVendorBarcode(Long vendorId, Long barcode) {

		InventoryResponse inventoryResponse = null;

		if (vendorId == null || barcode == null) {
			LOGGER.debug("Invalid request received for NULL vendor id or NULL barcode.");
			inventoryResponse = new InventoryResponse();
			inventoryResponse.setIsError(Boolean.TRUE);
			inventoryResponse.setVendorId(vendorId);

		} else {
			inventoryResponse = this.recentItemCache.getIfPresent(vendorId + Constants.CACHE_KEY_SEPARATOR_STRING
					+ barcode);
			LOGGER.debug("The latest for item with vendor [{}], barcode [{}]  is found and being returned.", vendorId,
					barcode);
		}

		return inventoryResponse;
	}

}
