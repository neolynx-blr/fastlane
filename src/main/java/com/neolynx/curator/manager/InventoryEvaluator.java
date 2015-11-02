package com.neolynx.curator.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.client.InventoryInfo;
import com.neolynx.curator.util.Constants;

public class InventoryEvaluator {

	private final LoadingCache<Long, Long> vendorVersionCache;
	static Logger LOGGER = LoggerFactory.getLogger(InventoryEvaluator.class);
	private final LoadingCache<String, InventoryInfo> differentialInventoryCache;
	private final LoadingCache<String, InventoryInfo> recentItemCache;
	private final LoadingCache<Long, String> currentInventoryCache;

	public InventoryEvaluator(LoadingCache<String, InventoryInfo> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache, LoadingCache<String, InventoryInfo> recentItemCache,
			LoadingCache<Long, String> currentInventoryCache) {
		super();
		this.differentialInventoryCache = differentialInventoryCache;
		this.vendorVersionCache = vendorVersionCache;
		this.recentItemCache = recentItemCache;
		this.currentInventoryCache = currentInventoryCache;
	}

	// Simply pull the data from the cache
	public InventoryInfo getInventoryDifferential(Long vendorId, Long dataVersionId) {

		InventoryInfo inventoryResponse = null;
		LOGGER.debug("Request received for inventory differential for vendor-version [{}-{}]", vendorId, dataVersionId);

		if (vendorId == null || dataVersionId == null) {
			LOGGER.debug("Invalid request received for missing vendor and/or version id.");

			inventoryResponse = new InventoryInfo();
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

	public InventoryInfo getLatestInventory(Long vendorId) {

		InventoryInfo inventoryResponse = null;

		if (vendorId == null) {
			LOGGER.debug("Invalid request received for NULL vendor id.");
			inventoryResponse = new InventoryInfo();
			inventoryResponse.setIsError(Boolean.TRUE);
			inventoryResponse.setVendorId(vendorId);

		} else {
			ObjectMapper mapper = new ObjectMapper();
			String latestInventory = this.currentInventoryCache.getIfPresent(vendorId);

			try {
				inventoryResponse = mapper.readValue(latestInventory, InventoryInfo.class);
			} catch (Exception e) {
				LOGGER.error("Unable to deserialize and return latest inventory for vendor [{}] with error message [{}]", vendorId, e.getMessage());

				e.printStackTrace();
				
				inventoryResponse = new InventoryInfo();
				inventoryResponse.setIsError(Boolean.TRUE);
				inventoryResponse.setVendorId(vendorId);

			}

			LOGGER.debug(
					"The latest version found for vendor [{}] is [{}], returning [{}] Added and [{}] Updated items.",
					vendorId, inventoryResponse.getNewDataVersionId(), inventoryResponse.getAddedItems().size(),
					inventoryResponse.getUpdatedItems().size());
		}

		return inventoryResponse;
	}

	public InventoryInfo getLatestItemForVendorBarcode(Long vendorId, Long barcode) {

		InventoryInfo inventoryResponse = null;

		if (vendorId == null || barcode == null) {
			LOGGER.debug("Invalid request received for NULL vendor id or NULL barcode.");
			inventoryResponse = new InventoryInfo();
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
