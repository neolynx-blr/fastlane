package com.example.helloworld.core;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.common.cache.CacheLoader;

/**
 * Created by nitesh.garg on 06-Sep-2015
 */
public class InventoryResponseCacheLoader extends CacheLoader<String, InventoryResponse> {

	private SessionFactory sessionFactory;

	public InventoryResponseCacheLoader(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private List<String> convertStringToTokens(String data) {
		return new StrTokenizer(data, ",").getTokenList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.common.cache.CacheLoader#load(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@UnitOfWork
	@Override
	public InventoryResponse load(String vendorVersionKey) throws Exception {

		InventoryResponse inventoryResponse = new InventoryResponse();
		List<String> vendorVersion = new StrTokenizer(vendorVersionKey, "-").getTokenList();

		Long vendorId = Long.parseLong(vendorVersion.get(0));
		Long versionId = Long.parseLong(vendorVersion.get(1));

		inventoryResponse.setVendorId(vendorId);
		inventoryResponse.setCurrentDataVersionId(versionId);

		Session session = sessionFactory.openSession();
		Query diffQuery = session
				.createSQLQuery(
						" select vvd.* from vendor_version_differential vvd where vendor_id = :vendorId and version_id = :versionId order by last_modified_on ")
				.addEntity("vendor_version_differential", VendorVersionDifferential.class)
				.setParameter("vendorId", vendorId).setParameter("versionId", versionId);

		List<VendorVersionDifferential> vendorVersionDifferentials = diffQuery.list();

		if (vendorVersionDifferentials.isEmpty()) {
			System.out.println("Unable to find DB entry for Vendor:Version," + vendorId + ":" + versionId);
		} else {

			List<VendorItemMaster> vendorItemMasterList;

			VendorVersionDifferential diffInstance = vendorVersionDifferentials.get(0);
			Long lastSyncedVersionId = diffInstance.getLastSyncedVersionId();

			inventoryResponse.setNewDataVersionId(lastSyncedVersionId);

			if (versionId.compareTo(lastSyncedVersionId) == 0) {

				Query query = session
						.createSQLQuery(
								"select vim.* from vendor_item_master vim where vendor_id = :vendorId "
										+ " order by vim.item_code ")
						.addEntity("vendor_item_master", VendorItemMaster.class).setParameter("vendorId", vendorId);

				vendorItemMasterList = query.list();
				System.out.println("For Vendor ::" + vendorId + " pulling the latest inventory containing "
						+ vendorItemMasterList.size() + " items.");

			} else {

				Query query = session
						.createSQLQuery(
								"select vim.* from vendor_item_master vim where vendor_id = :vendorId and item_code in (:itemCodes) "
										+ " order by vim.item_code ")
						.addEntity("vendor_item_master", VendorItemMaster.class).setParameter("vendorId", vendorId)
						.setParameter("itemCodes", convertStringToTokens(diffInstance.getDeltaItemCodes()));

				vendorItemMasterList = query.list();
				System.out.println("For Vendor ::" + vendorId + " found " + vendorItemMasterList.size()
						+ " items for differential.");

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
		}

		session.close();
		return inventoryResponse;
	}

}
