package com.example.helloworld.manager;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.ItemResponse;
import com.example.helloworld.core.VendorItemMaster;
import com.example.helloworld.core.VendorVersionDifferential;
import com.google.common.cache.LoadingCache;

/**
 * Created by nitesh.garg on 06-Sep-2015
 */
public class CacheSetup implements Runnable {

	private final SessionFactory sessionFactory;
	private final LoadingCache<Long, Long> vendorVersionCache;
	private final LoadingCache<String, InventoryResponse> differentialInventoryCache;

	public CacheSetup(SessionFactory sessionFactory,
			LoadingCache<String, InventoryResponse> differentialInventoryCache, LoadingCache<Long, Long> vendorVersionCache) {
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

				System.out.println("Wokeup with cache-size of {" + this.differentialInventoryCache.size()
						+ "}");

				Session session = sessionFactory.openSession();
				Query diffQuery = session.createSQLQuery(
						"select vvd.* from vendor_version_differential vvd order by last_modified_on ").addEntity(
						"vendor_version_differential", VendorVersionDifferential.class);

				List<VendorVersionDifferential> vendorVersionDifferentials = diffQuery.list();

				for (VendorVersionDifferential diffInstance : vendorVersionDifferentials) {

					InventoryResponse inventoryResponse = new InventoryResponse();

					Long vendorId = diffInstance.getVendorId();
					Long versionId = diffInstance.getVersionId();
					Long lastSyncedVersionId = diffInstance.getLastSyncedVersionId();
					
					if (this.vendorVersionCache.getIfPresent(vendorId).compareTo(lastSyncedVersionId) == 0) {
						System.out.println("Skipping the cache refresh of {" + vendorId + "-" + versionId
								+ "} entries.");
						continue;
					} else {
						System.out.println("Time to refresh cache for Vendor:Version {" + vendorId + ":" + versionId
								+ "} as cache has version {" + this.vendorVersionCache.getIfPresent(vendorId)
								+ "} as against latest {" + lastSyncedVersionId + "}");
					}

					inventoryResponse.setVendorId(vendorId);
					inventoryResponse.setCurrentDataVersionId(versionId);
					inventoryResponse.setNewDataVersionId(lastSyncedVersionId);

					List<VendorItemMaster> vendorItemMasterList;
					if (versionId.compareTo(lastSyncedVersionId) == 0) {

						Query query = session
								.createSQLQuery(
										"select vim.* from vendor_item_master vim where vendor_id = :vendorId "
												+ " order by vim.item_code ")
								.addEntity("vendor_item_master", VendorItemMaster.class)
								.setParameter("vendorId", vendorId);

						vendorItemMasterList = query.list();
						System.out.println("For Vendor ::" + vendorId + " pulling the latest inventory containing "
								+ vendorItemMasterList.size() + " items.");

					} else {

						Query query = session
								.createSQLQuery(
										"select vim.* from vendor_item_master vim where vendor_id = :vendorId and item_code in (:itemCodes) "
												+ " order by vim.item_code ")
								.addEntity("vendor_item_master", VendorItemMaster.class)
								.setParameter("vendorId", vendorId)
								.setParameter("itemCodes", convertStringToTokens(diffInstance.getDeltaItemCodes()));

						vendorItemMasterList = query.list();
						System.out.println("For Vendor ::" + vendorId + " found " + vendorItemMasterList.size()
								+ " items for differential.");

					}

					if(!vendorItemMasterList.isEmpty()) {
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
				System.out.println("Leaving with cache-size of {" + this.differentialInventoryCache.size()
						+ "} entries.");

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
