package com.example.helloworld.manager;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.ItemResponse;
import com.example.helloworld.core.VendorItemMaster;
import com.example.helloworld.core.VendorVersionDetail;
import com.example.helloworld.core.VendorVersionDifferential;
import com.google.common.cache.LoadingCache;

/**
 * Created by nitesh.garg on 06-Sep-2015
 */
public class CacheSetup implements Runnable {

	private final SessionFactory sessionFactory;
	private final LoadingCache<Long, Long> vendorVersionCache;
	private final LoadingCache<String, InventoryResponse> differentialInventoryCache;

	static Logger LOGGER = LoggerFactory.getLogger(CacheSetup.class);

	public CacheSetup(SessionFactory sessionFactory,
			LoadingCache<String, InventoryResponse> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache) {
		super();
		this.sessionFactory = sessionFactory;
		this.vendorVersionCache = vendorVersionCache;
		this.differentialInventoryCache = differentialInventoryCache;
	}

	private List<String> convertStringToTokens(String data) {
		return new StrTokenizer(data, ",").getTokenList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@UnitOfWork
	public void run() {

		// Keeping running for ever in the background looking for new inventory
		while (true) {

			try {

				// Check for new inventory every 'X' seconds.
				Thread.sleep(10000);

				LOGGER.debug("Wokeup with differential cache-size of [{}] and vendor-version cache-size of [{}]",
						this.differentialInventoryCache.size(), this.vendorVersionCache.size());

				Session session = sessionFactory.openSession();

				/*
				 * Before anything else, looks at the vendor-version cache to
				 * ensure that all latest versions are correctly recorded
				 */
				Query vendorDetailQuery = session.createSQLQuery(" select vvd.* from vendor_version_detail vvd ")
						.addEntity("vendor_version_detail", VendorVersionDetail.class);

				List<VendorVersionDetail> vendorVersionDetails = vendorDetailQuery.list();

				for (VendorVersionDetail instance : vendorVersionDetails) {
					Long vendorId = instance.getVendorId();

					Long actualLatestVersion = instance.getLatestSyncedVersionId();
					Long cachedLatestVersion = this.vendorVersionCache.getIfPresent(vendorId);

					LOGGER.debug("While checking latest version for vendor [{}], found [{}] in cache and [{}] in DB",
							vendorId, cachedLatestVersion, actualLatestVersion);
					if (cachedLatestVersion.compareTo(actualLatestVersion) != 0) {
						this.vendorVersionCache.put(vendorId, actualLatestVersion);
						LOGGER.debug("Updated the cache with vendor-version [{}-{}]", vendorId, actualLatestVersion);
					}

				}

				Query diffQuery = session.createSQLQuery(
						"select vvd.* from vendor_version_differential vvd order by last_modified_on ").addEntity(
						"vendor_version_differential", VendorVersionDifferential.class);

				List<VendorVersionDifferential> vendorVersionDifferentials = diffQuery.list();

				/*
				 * Basically iterate over all the latest differential data from
				 * DB, and check in last synced data is same as latest version
				 * for that vendor known in the cache. If same, do nothing, or
				 * else pull the data for latest version from DB and update the
				 * cache.
				 */
				for (VendorVersionDifferential diffInstance : vendorVersionDifferentials) {

					InventoryResponse inventoryResponse = new InventoryResponse();

					Long vendorId = diffInstance.getVendorId();
					Long versionId = diffInstance.getVersionId();
					Long lastSyncedVersionId = diffInstance.getLastSyncedVersionId();

					/*
					 * If the latest known version for vendor same as for which
					 * the differential is built?
					 */
					if (this.vendorVersionCache.getIfPresent(vendorId).compareTo(lastSyncedVersionId) == 0) {
						LOGGER.debug("Skipping the differential cache refresh of vendor-version [{}-{}]", vendorId, versionId);
						// If so, skip
						continue;
					} else {
						LOGGER.debug(
								"Time to refresh differential cache for vendor-version [{}-{}] as cache has version [{}] as against latest",
								vendorId, versionId, this.vendorVersionCache.getIfPresent(vendorId),
								lastSyncedVersionId);
					}

					// Otherwise, update cache
					inventoryResponse.setVendorId(vendorId);
					inventoryResponse.setCurrentDataVersionId(versionId);
					inventoryResponse.setNewDataVersionId(lastSyncedVersionId);

					List<VendorItemMaster> vendorItemMasterList;

					/*
					 * Now in the differential row of DB, if the version for
					 * which differential is stored is same as last sync
					 * version, that means that was the last latest known
					 * version and the differential will be none. Now, for such
					 * cases, simply pick everything that was added to
					 * vendor_item_master since that last sync version.
					 * 
					 * If the two versions are different, look for all
					 * previously known differential and added differentials
					 * since last synced version.
					 */
					if (versionId.compareTo(lastSyncedVersionId) == 0) {

						Query query = session
								.createSQLQuery(
										"select vim.* from vendor_item_master vim where vendor_id = :vendorId where version_id > :lastSyncedVersionId "
												+ " order by vim.item_code ")
								.addEntity("vendor_item_master", VendorItemMaster.class)
								.setParameter("lastSyncedVersionId", lastSyncedVersionId)
								.setParameter("vendorId", vendorId);

						vendorItemMasterList = query.list();
						LOGGER.debug(
								"For vendor [{}] updating the last latest version row, hence pulling all [{}] the recently added rows, that is since version [{}]",
								vendorId, vendorItemMasterList.size(), lastSyncedVersionId);

					} else {

						Query query = session
								.createSQLQuery(
										"select vim.* from vendor_item_master vim where vendor_id = :vendorId and (item_code in (:itemCodes) or version_id > :lastSyncedVersionId) "
												+ " order by vim.item_code ")
								.addEntity("vendor_item_master", VendorItemMaster.class)
								.setParameter("vendorId", vendorId)
								.setParameter("lastSyncedVersionId", lastSyncedVersionId)
								.setParameter("itemCodes", convertStringToTokens(diffInstance.getDeltaItemCodes()));

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
				LOGGER.debug("Leaving with cache-size of [{}] entries", this.differentialInventoryCache.size());

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
