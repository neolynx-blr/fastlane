package com.neolynx.curator.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.InventoryResponse;
import com.neolynx.common.model.ItemResponse;
import com.neolynx.curator.core.VendorItemMaster;
import com.neolynx.curator.core.VendorVersionDetail;
import com.neolynx.curator.core.VendorVersionDifferential;
import com.neolynx.curator.util.Constants;
import com.neolynx.curator.util.StringUtilsCustom;

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
	private final LoadingCache<String, InventoryResponse> differentialInventoryCache;

	static Logger LOGGER = LoggerFactory.getLogger(CacheCurator.class);

	public CacheCurator(SessionFactory sessionFactory,
			LoadingCache<String, InventoryResponse> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache) {
		super();
		this.sessionFactory = sessionFactory;
		this.differentialInventoryCache = differentialInventoryCache;
		this.vendorVersionCache = vendorVersionCache;
	}

	@SuppressWarnings("unchecked")
	public void processVendorVersionCache() {

		Session session = sessionFactory.openSession();

		/*
		 * Before anything else, looks at the vendor-version cache to ensure
		 * that all latest versions are correctly recorded. If not, update the
		 * same in the cache.
		 */

		Query vendorDetailQuery = session.createSQLQuery(" select vvd.* from vendor_version_detail vvd ").addEntity(
				"vendor_version_detail", VendorVersionDetail.class);

		List<VendorVersionDetail> vendorVersionDetails = vendorDetailQuery.list();

		for (VendorVersionDetail instance : vendorVersionDetails) {
			Long vendorId = instance.getVendorId();

			Long actualLatestVersion = instance.getLatestSyncedVersionId();
			Long cachedLatestVersion = this.vendorVersionCache.getIfPresent(vendorId);

			LOGGER.debug("While checking latest version for vendor [{}], found [{}] in cache and [{}] in DB", vendorId,
					cachedLatestVersion, actualLatestVersion);

			// If nothing or different version in cache, load fresh
			if (cachedLatestVersion == null || cachedLatestVersion.compareTo(actualLatestVersion) != 0) {
				this.vendorVersionCache.put(vendorId, actualLatestVersion);
				LOGGER.debug("Updated the cache with vendor-version [{}-{}]", vendorId, actualLatestVersion);
			}

		}

		session.close();
	}

	public void removeDifferentialInventoryCache(Long vendorId) {

		int invalidateCount = 0;

		Set<String> differentialCacheKeys = this.differentialInventoryCache.asMap().keySet();
		LOGGER.debug(
				"Will iterate over [{}] keys in the differential inventory cache to remove for all keys for vendor [{}]",
				differentialCacheKeys.size(), vendorId);

		for (String key : differentialCacheKeys) {

			if (StringUtils.startsWith(key, String.valueOf(vendorId) + Constants.VENDOR_VERSION_KEY_SEPARATOR)) {
				invalidateCount++;
				LOGGER.debug("Removing key [{}] from the differential inventory cache", key);
				this.differentialInventoryCache.invalidate(key);
			}

		}

		LOGGER.debug("End up removing [{}] keys from the differential inventory cache.", invalidateCount);

	}

	@SuppressWarnings("unchecked")
	public void processDifferentialInventoryCache() {

		Session session = sessionFactory.openSession();
		Query diffQuery = session.createSQLQuery(
				"select vvd.* from vendor_version_differential vvd order by last_modified_on ").addEntity(
				"vendor_version_differential", VendorVersionDifferential.class);

		List<VendorVersionDifferential> vendorVersionDifferentials = diffQuery.list();

		/*
		 * Basically iterate over all the latest differential data from DB, and
		 * check in last synced data is same as latest version for that vendor
		 * known in the cache. If same, do nothing, or else pull the data for
		 * latest version from DB and update the cache.
		 */
		for (VendorVersionDifferential diffInstance : vendorVersionDifferentials) {

			InventoryResponse inventoryResponse = new InventoryResponse();

			Long vendorId = diffInstance.getVendorId();
			Long versionId = diffInstance.getVersionId();
			Long lastSyncedVersionId = diffInstance.getLastSyncedVersionId();

			/*
			 * If the latest known version for vendor same as for which the
			 * differential is built?
			 */

			String key = vendorId + Constants.VENDOR_VERSION_KEY_SEPARATOR + versionId;
			InventoryResponse cachedEntry = this.differentialInventoryCache.getIfPresent(key);

			if (cachedEntry != null && cachedEntry.getNewDataVersionId().compareTo(lastSyncedVersionId) == 0) {
				LOGGER.debug(
						"Skipping the differential cache refresh of vendor-version [{}-{}] to latest version [{}].",
						vendorId, versionId, lastSyncedVersionId);
				continue;
			} else {
				LOGGER.debug(
						"Time to refresh differential cache for vendor-version [{}-{}] as cache has version [{}] as against latest [{}]",
						vendorId, versionId, cachedEntry == null ? "Null" : cachedEntry.getNewDataVersionId(),
						lastSyncedVersionId);
			}

			// Otherwise, update cache
			inventoryResponse.setVendorId(vendorId);
			inventoryResponse.setCurrentDataVersionId(versionId);
			inventoryResponse.setNewDataVersionId(lastSyncedVersionId);

			List<VendorItemMaster> vendorItemMasterList;

			/*
			 * Now in the differential row of DB, if the version for which
			 * differential is stored is same as last sync version, that means
			 * that was the last latest known version and the differential will
			 * be none. Now, for such cases, simply pick everything that was
			 * added to vendor_item_master since that last sync version.
			 * 
			 * If the two versions are different, look for all previously known
			 * differential and added differentials since last synced version.
			 */
			if (versionId.compareTo(lastSyncedVersionId) == 0) {

				Query query = session
						.createSQLQuery(
								"select vim.* from vendor_item_master vim where vendor_id = :vendorId and version_id <= :lastSyncedVersionId "
										+ " order by vim.item_code ")
						.addEntity("vendor_item_master", VendorItemMaster.class)
						.setParameter("lastSyncedVersionId", lastSyncedVersionId).setParameter("vendorId", vendorId);

				LOGGER.debug("Query Executed [{}]", query.toString());

				vendorItemMasterList = query.list();
				LOGGER.debug(
						"For vendor [{}] updating the last latest version row, hence pulling all [{}] the recently added rows, that is since version [{}]",
						vendorId, vendorItemMasterList.size(), lastSyncedVersionId);

			} else {

				Query query = session
						.createSQLQuery(
								"select vim.* from vendor_item_master vim where vendor_id = :vendorId and ( item_code in (:itemCodes) or (version_id > :versionId and version_id <= :lastSyncedVersionId)) "
										+ " order by vim.item_code ")
						.addEntity("vendor_item_master", VendorItemMaster.class)
						.setParameter("vendorId", vendorId)
						.setParameter("versionId", versionId)
						.setParameter("lastSyncedVersionId", lastSyncedVersionId)
						.setParameterList("itemCodes",
								StringUtilsCustom.convertStringToTokens(diffInstance.getDeltaItemCodes()));

				LOGGER.debug("Query Executed [{}]", query.toString());

				vendorItemMasterList = query.list();
				LOGGER.debug(
						"For vendor [{}], keeping track of existing differential and newly created rows since the last synced version {{}], overall pulling all [{}] rows",
						vendorId, lastSyncedVersionId, vendorItemMasterList.size());

			}

			if (!vendorItemMasterList.isEmpty()) {
				inventoryResponse.setItemsUpdated(new ArrayList<ItemResponse>());
			}

			for (VendorItemMaster vendorItemData : vendorItemMasterList) {

				ItemResponse itemData = new ItemResponse();
				itemData.setBarcode(vendorItemData.getBarcode());
				itemData.setDescription(vendorItemData.getDescription());
				itemData.setDiscountType(vendorItemData.getDiscountType());
				itemData.setDiscountValue(vendorItemData.getDiscountValue());
				itemData.setImageJSON(vendorItemData.getImageJSON());
				itemData.setItemCode(vendorItemData.getItemCode());
				itemData.setMrp(vendorItemData.getMrp());
				itemData.setName(vendorItemData.getName());
				itemData.setPrice(vendorItemData.getPrice());
				itemData.setProductId(vendorItemData.getProductId());
				itemData.setTagline(vendorItemData.getTagLine());
				itemData.setVersionId(vendorItemData.getVersionId());

				inventoryResponse.getItemsUpdated().add(itemData);

			}

			this.differentialInventoryCache.put(vendorId + "-" + versionId, inventoryResponse);
		}

		session.close();

	}

}