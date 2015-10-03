package com.neolynx.curator.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheLoader;
import com.neolynx.common.model.InventoryResponse;
import com.neolynx.common.model.ItemResponse;
import com.neolynx.curator.core.VendorItemMaster;
import com.neolynx.curator.util.Constants;

/**
 * Created by nitesh.garg on Oct 3, 2015
 */
public class RecentItemLoader extends CacheLoader<String, InventoryResponse> {

	private SessionFactory sessionFactory;
	static Logger LOGGER = LoggerFactory.getLogger(RecentItemLoader.class);

	public RecentItemLoader(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.common.cache.CacheLoader#load(java.lang.Object)
	 */
	@Override
	public InventoryResponse load(String vendorBarcodeKey) throws Exception {

		if (vendorBarcodeKey == null) {
			LOGGER.debug("Tried loading recent data for NULL vendor-barcode-id combination, obviously failed.");
			return null;
		}

		InventoryResponse inventoryResponse = new InventoryResponse();
		List<String> vendorBarcode = new StrTokenizer(vendorBarcodeKey, Constants.CACHE_KEY_SEPARATOR_STRING)
				.getTokenList();

		Long vendorId = Long.parseLong(vendorBarcode.get(0));
		Long barcode = Long.parseLong(vendorBarcode.get(1));

		LOGGER.debug("Looking to load recent data into cache for vendor-barcode [{}-{}]", vendorId, barcode);

		Session session = sessionFactory.openSession();

		// Check the DB data for this vendor-version combination
		Query recentDataQuery = session
				.createSQLQuery(
						" select vim.* from vendor_item_master vim where vendor_id = :vendorId and barcode = :barcode ")
				.addEntity("vendor_item_master", VendorItemMaster.class).setParameter("vendorId", vendorId)
				.setParameter("barcode", barcode);

		@SuppressWarnings("unchecked")
		List<VendorItemMaster> recentVendorBarcodeRecord = recentDataQuery.list();

		if (CollectionUtils.isEmpty(recentVendorBarcodeRecord)) {
			LOGGER.debug("Unable to find DB entry for Vendor-Barcode [{}-{}]", vendorId, barcode);
		} else {

			VendorItemMaster vendorItemData = recentVendorBarcodeRecord.get(0);

			inventoryResponse.setVendorId(vendorId);
			inventoryResponse.setCurrentDataVersionId(vendorItemData.getVersionId());
			inventoryResponse.setNewDataVersionId(vendorItemData.getVersionId());
			inventoryResponse.setItemsUpdated(new ArrayList<ItemResponse>());

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
			LOGGER.debug("Adding recent data with item-code [{}] for vendor-version-barcode [{}-{}-{}]",
					itemData.getItemCode(), vendorId, itemData.getVersionId(), barcode);
		}

		session.close();
		inventoryResponse.setIsError(Boolean.FALSE);
		return inventoryResponse;
	}

}
