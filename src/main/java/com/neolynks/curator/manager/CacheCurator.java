package com.neolynks.curator.manager;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.LoadingCache;
import com.neolynks.common.model.client.InventoryInfo;
import com.neolynks.common.model.client.ItemInfo;
import com.neolynks.common.model.client.ProductInfo;
import com.neolynks.common.model.client.price.DiscountDetail;
import com.neolynks.common.model.client.price.ItemPrice;
import com.neolynks.common.model.client.price.TaxDetail;
import com.neolynks.curator.core.VendorItemMaster;
import com.neolynks.curator.core.VendorVersionDetail;
import com.neolynks.curator.core.VendorVersionDifferential;
import com.neolynks.curator.util.Constants;

/**
 * Used for initial cache data loading. Simply get all data rows in the
 * vendor_version_detail and vendor_version_differential tables and call the get
 * on cache which loads the data if that is absent.
 * 
 * Created by nitesh.garg on 07-Sep-2015
 */
public class CacheCurator {

	private final SessionFactory sessionFactory;
	private final LoadingCache<Long, Long> vendorVersionCache;
	private final LoadingCache<Long, String> currentInventoryCache;
	private final LoadingCache<String, InventoryInfo> recentItemsCache;
	private final LoadingCache<String, InventoryInfo> differentialInventoryCache;

	static Logger LOGGER = LoggerFactory.getLogger(CacheCurator.class);

	public CacheCurator(SessionFactory sessionFactory, LoadingCache<String, InventoryInfo> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache, LoadingCache<String, InventoryInfo> recentItemsCache,
			LoadingCache<Long, String> currentInventoryCache) {
		super();
		this.sessionFactory = sessionFactory;
		this.differentialInventoryCache = differentialInventoryCache;
		this.vendorVersionCache = vendorVersionCache;
		this.recentItemsCache = recentItemsCache;
		this.currentInventoryCache = currentInventoryCache;
	}

	public void processVendorVersionCache() {

		Session session = sessionFactory.openSession();

		/*
		 * Before anything else, looks at the vendor-version cache to ensure
		 * that all latest versions are correctly recorded. If not, update the
		 * same in the cache.
		 */

		try {
			Query vendorDetailQuery = session.createSQLQuery(" select vvd.* from vendor_version_detail vvd ")
					.addEntity("vendor_version_detail", VendorVersionDetail.class);

			@SuppressWarnings("unchecked")
			List<VendorVersionDetail> vendorVersionDetails = vendorDetailQuery.list();

			for (VendorVersionDetail instance : vendorVersionDetails) {
				Long vendorId = instance.getVendorId();

				Long actualLatestVersion = instance.getLatestSyncedVersionId();
				Long cachedLatestVersion = this.vendorVersionCache.getIfPresent(vendorId);

				LOGGER.trace("While checking latest version for vendor [{}], found [{}] in cache and [{}] in DB",
						vendorId, cachedLatestVersion, actualLatestVersion);

				// If nothing or different version in cache, load fresh
				if (cachedLatestVersion == null || cachedLatestVersion.compareTo(actualLatestVersion) != 0) {
					this.vendorVersionCache.put(vendorId, actualLatestVersion);
					LOGGER.debug("Updated the vendor-verison for vendor [{}] from version [{}] to [{}]", vendorId,
							cachedLatestVersion, actualLatestVersion);
				}

			}
		} catch (Exception e) {
			LOGGER.warn(
					"Exception [{}] with message [{}] occurred while generating data for differential data. Eating the exception for it will be retried soon.",
					e.getClass().getName(), e.getMessage());
			e.printStackTrace();
		} finally {
			session.close();
		}

	}

	public void processCurrentInventoryCache() {

		Session session = sessionFactory.openSession();

		try {
			Query vendorDetailQuery = session.createSQLQuery(
					" select vvd.* from vendor_version_detail vvd where latest_synced_version_id > 0 ").addEntity(
					"vendor_version_detail", VendorVersionDetail.class);

			@SuppressWarnings("unchecked")
			List<VendorVersionDetail> vendorVersionDetails = vendorDetailQuery.list();

			for (VendorVersionDetail instance : vendorVersionDetails) {

				Long vendorId = instance.getVendorId();
				Long actualLatestVersion = instance.getLatestSyncedVersionId();

				LOGGER.trace("Evaluating current inventory cache updates for vendor [{}] to latest version [{}]",
						vendorId, actualLatestVersion);

				ObjectMapper mapper = new ObjectMapper();

				String currentInventoryData = this.currentInventoryCache.getIfPresent(vendorId);
				if (currentInventoryData != null) {

					InventoryInfo cachedInventoryInfo = null;

					try {
						cachedInventoryInfo = mapper.readValue(currentInventoryData, InventoryInfo.class);
						LOGGER.debug(
								"While checking latest inventory version for vendor [{}], found [{}] in cache and [{}] in DB",
								vendorId, cachedInventoryInfo.getNewDataVersionId(), actualLatestVersion);
						if (cachedInventoryInfo.getNewDataVersionId().compareTo(actualLatestVersion) == 0) {
							continue;
						}
					} catch (Exception ex) {
						LOGGER.warn(
								"Exception [{}] with meesage [{}] occurred while deserializing the current inventory in cache for vendor [{}]. Will call for refresh.",
								ex.getClass().getName(), ex.getMessage(), vendorId);
						ex.printStackTrace();
					}

				}

				LOGGER.info("Asking for current inventory cache refresh for vendor [{}] to version [{}]", vendorId,
						actualLatestVersion);
				this.currentInventoryCache.refresh(vendorId);

			}

		} catch (Exception e) {
			LOGGER.warn(
					"Exception [{}] with message [{}] occurred while generating data for differential data. Eating the exception for it will be retried soon.",
					e.getClass().getName(), e.getMessage());
			e.printStackTrace();
		} finally {
			session.close();
		}

	}

	@SuppressWarnings("unchecked")
	public void processDifferentialInventoryCache() {

		Session session = sessionFactory.openSession();
		try {
			Query diffQuery = session
					.createSQLQuery(
							"select vvd.* from vendor_version_differential vvd where last_synced_version_id != 0 and is_valid = 't' and is_this_latest_version != 't' order by last_modified_on ")
					.addEntity("vendor_version_differential", VendorVersionDifferential.class);

			List<VendorVersionDifferential> vendorVersionDifferentials = diffQuery.list();

			/*
			 * Basically iterate over all the latest differential data from DB,
			 * and check in last synced data is same as latest version for that
			 * vendor known in the cache. If same, do nothing, or else pull the
			 * data for latest version from DB and update the cache.
			 */
			for (VendorVersionDifferential diffInstance : vendorVersionDifferentials) {

				Long vendorId = diffInstance.getVendorId();
				Long versionId = diffInstance.getVersionId();
				Long lastSyncedVersionId = diffInstance.getLastSyncedVersionId();

				/*
				 * If the latest known version for vendor same as for which the
				 * differential is built?
				 */

				ObjectMapper mapper = new ObjectMapper();
				String key = vendorId + Constants.CACHE_KEY_SEPARATOR_STRING + versionId;
				InventoryInfo cachedEntry = this.differentialInventoryCache.getIfPresent(key);

				if (cachedEntry == null || cachedEntry.getNewDataVersionId() == null
						|| cachedEntry.getNewDataVersionId().compareTo(lastSyncedVersionId) != 0) {

					LOGGER.debug(
							"Time to refresh differential cache for vendor-version [{}-{}] as cache has version [{}] as against latest [{}]",
							vendorId, versionId, cachedEntry == null ? "Null" : cachedEntry.getNewDataVersionId(),
							lastSyncedVersionId);
					try {
						this.differentialInventoryCache.put(key,
								mapper.readValue(diffInstance.getDifferentialData(), InventoryInfo.class));
					} catch (IOException e) {
						LOGGER.error(
								"Received error [{}] while deserializing the InventoryInfo from the DB while building differential cache for vendor [{}], version [{}] against last sync version [{}]",
								e.getMessage(), vendorId, versionId, lastSyncedVersionId);
					}
					continue;
				}

				LOGGER.debug(
						"Skipping the differential cache refresh of vendor-version [{}-{}] to latest version [{}].",
						vendorId, versionId, lastSyncedVersionId);
			}

		} catch (Exception e) {
			LOGGER.warn(
					"Exception [{}] with message [{}] occurred while generating data for differential data. Eating the exception for it will be retried soon.",
					e.getClass().getName(), e.getMessage());
			e.printStackTrace();
		} finally {
			session.close();
		}

	}

	@SuppressWarnings("unchecked")
	public void processRecentItemRecordsCache() {

		Session session = sessionFactory.openSession();
		try {
			Query diffQuery = session
					.createSQLQuery(
							"select vim.* from vendor_item_master vim where created_on > current_timestamp - interval '1 days' order by created_on ")
					.addEntity("vendor_item_master", VendorItemMaster.class);

			List<VendorItemMaster> recentVendorItemData = diffQuery.list();
			LOGGER.debug("Found [{}] records created in last day that would be added to the recent-items cache",
					recentVendorItemData.size());

			/*
			 * TODO For now not worrying about a fresh load of full inventory
			 * that could have taken place in last 1 day. All that inventory
			 * will get loaded.
			 */
			for (VendorItemMaster vendorItemData : recentVendorItemData) {

				InventoryInfo inventoryResponse = new InventoryInfo();

				Long vendorId = vendorItemData.getVendorId();
				String barcode = vendorItemData.getBarcode();
				Long lastSyncedVersionId = vendorItemData.getVersionId();

				String key = vendorId + Constants.CACHE_KEY_SEPARATOR_STRING + barcode;
				InventoryInfo cachedEntry = this.recentItemsCache.getIfPresent(key);

				if (cachedEntry != null && cachedEntry.getNewDataVersionId().compareTo(lastSyncedVersionId) == 0) {
					LOGGER.debug(
							"Skipping the recent item cache refresh of vendor-barcode [{}-{}] to latest version [{}].",
							vendorId, barcode, lastSyncedVersionId);
					continue;
				} else {
					LOGGER.debug(
							"Refreshing item cache for vendor-barcode [{}-{}] as older version [{}] against latest [{}]",
							vendorId, barcode, cachedEntry == null ? "Null" : cachedEntry.getNewDataVersionId(),
							lastSyncedVersionId);
				}

				// Otherwise, update cache
				inventoryResponse.setVendorId(vendorId);
				inventoryResponse.setNewDataVersionId(lastSyncedVersionId);
				inventoryResponse.setCurrentDataVersionId(lastSyncedVersionId);
				//inventoryResponse.setUpdatedItems(new HashMap<String, ItemInfo>());

				ItemInfo itemInfo = new ItemInfo();
				ProductInfo productInfo = new ProductInfo();
				ItemPrice itemPrice = new ItemPrice();

				productInfo.setName(vendorItemData.getName());
				productInfo.setTagLine(vendorItemData.getTagLine());
				productInfo.setImageJSON(vendorItemData.getImageJSON());
				productInfo.setDescription(vendorItemData.getDescription());

				try {
					ObjectMapper mapper = new ObjectMapper();

					itemPrice.setTaxDetail(StringUtils.isNotEmpty(vendorItemData.getTaxJSON()) ? mapper.readValue(
							vendorItemData.getTaxJSON(), TaxDetail.class) : null);
					itemPrice.setDiscountDetail(StringUtils.isNotEmpty(vendorItemData.getDiscountJSON()) ? mapper
							.readValue(vendorItemData.getDiscountJSON(), DiscountDetail.class) : null);

				} catch (Exception e) {
					LOGGER.warn(
							"Exception [{}] with message [{}] occurred while generating data for differential data. Eating the exception for it will be retried soon.",
							e.getClass().getName(), e.getMessage());
					e.printStackTrace();
				}

				itemPrice.setMrp(vendorItemData.getMrp());
				itemPrice.setPrice(vendorItemData.getPrice());

				itemInfo.setItemPrice(itemPrice);
				itemInfo.setProductInfo(productInfo);

				// TODO Fixit
				itemInfo.setBarcode(Long.parseLong(vendorItemData.getBarcode()));
				itemInfo.setItemCode(vendorItemData.getItemCode());

				inventoryResponse.getAddedItems().put(itemInfo.getItemCode(), itemInfo);

				String newKey = vendorId + Constants.CACHE_KEY_SEPARATOR_STRING + barcode;

				LOGGER.info("Asking for recent-items cache refresh for key [{}]", newKey);
				this.recentItemsCache.put(newKey, inventoryResponse);
			}

		} catch (Exception e) {
			LOGGER.warn(
					"Exception [{}] with message [{}] occurred while generating data for differential data. Eating the exception for it will be retried soon.",
					e.getClass().getName(), e.getMessage());
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

	public void removeVersionCacheForVendor(Long vendorId) {
		// TODO Handle failure scenarios
		this.vendorVersionCache.invalidate(vendorId);
	}

	public void removeDifferentialInventoryCache(Long vendorId) {

		int invalidateCount = 0;

		Set<String> differentialCacheKeys = this.differentialInventoryCache.asMap().keySet();
		LOGGER.debug(
				"Will iterate over [{}] keys in the differential inventory cache to remove for all keys for vendor [{}]",
				differentialCacheKeys.size(), vendorId);

		for (String key : differentialCacheKeys) {

			if (StringUtils.startsWith(key, String.valueOf(vendorId) + Constants.CACHE_KEY_SEPARATOR_STRING)) {
				invalidateCount++;
				LOGGER.debug("Removing key [{}] from the differential inventory cache", key);
				this.differentialInventoryCache.invalidate(key);
			}

		}

		LOGGER.debug("End up removing [{}] keys from the differential inventory cache.", invalidateCount);

	}
}
