package com.neolynks.curator.cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jadira.usertype.spi.utils.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheLoader;
import com.neolynks.common.model.client.InventoryInfo;
import com.neolynks.common.model.client.ItemInfo;
import com.neolynks.common.model.client.ProductInfo;
import com.neolynks.common.model.client.price.DiscountDetail;
import com.neolynks.common.model.client.price.ItemPrice;
import com.neolynks.common.model.client.price.TaxDetail;
import com.neolynks.curator.core.VendorItemMaster;
import com.neolynks.curator.util.Constants;

/**
 * Created by nitesh.garg on Oct 3, 2015
 */
public class RecentItemLoader extends CacheLoader<String, InventoryInfo> {

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
	public InventoryInfo load(String vendorBarcodeKey) throws Exception {

		if (vendorBarcodeKey == null) {
			LOGGER.debug("Tried loading recent data for NULL vendor-barcode-id combination, obviously failed.");
			return null;
		}

		InventoryInfo inventoryResponse = new InventoryInfo();
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
			inventoryResponse.setUpdatedItems(new HashMap<String, ItemInfo>());

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
				
			} catch (IOException e) {
				LOGGER.error("Received error [{}] while deserializin the tax or discount JSONs [{}], [{}]",
						vendorItemData.getTaxJSON().trim(), vendorItemData.getDiscountJSON().trim(), e.getMessage());
			}

			itemPrice.setMrp(vendorItemData.getMrp());
			itemPrice.setPrice(vendorItemData.getPrice());

			itemInfo.setItemPrice(itemPrice);
			itemInfo.setProductInfo(productInfo);

			itemInfo.setBarcode(Long.parseLong(vendorItemData.getBarcode()));
			itemInfo.setItemCode(vendorItemData.getItemCode());

			inventoryResponse.getAddedItems().put(itemInfo.getItemCode(), itemInfo);
			LOGGER.debug("Adding recent data with item-code [{}] for vendor-version-barcode [{}-{}-{}]",
					itemInfo.getItemCode(), vendorId, inventoryResponse.getNewDataVersionId(), barcode);
		}

		session.close();
		inventoryResponse.setIsError(Boolean.FALSE);
		return inventoryResponse;
	}

}
