package com.example.helloworld.core;

import java.util.List;

import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.example.helloworld.db.VendorVersionDifferentialDAO;
import com.google.common.cache.CacheLoader;

/**
 * Created by nitesh.garg on 06-Sep-2015
 */
public class InventoryResponseCacheLoader extends CacheLoader<String, InventoryResponse> {

	private SessionFactory sessionFactory;
	private VendorVersionDifferentialDAO vendorVersionDifferentialDAO;

	public InventoryResponseCacheLoader(SessionFactory sessionFactory,
			VendorVersionDifferentialDAO vendorVersionDifferentialDAO) {
		this.sessionFactory = sessionFactory;
		this.vendorVersionDifferentialDAO = vendorVersionDifferentialDAO;
		setupInitialCache();
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
	@Override
	public InventoryResponse load(String vendorVersionKey) throws Exception {

		InventoryResponse inventoryResponse = null;
		List<String> vendorVersion = new StrTokenizer(vendorVersionKey, "-").getTokenList();

		Session session = this.sessionFactory.openSession();
		VendorVersionDifferential vendorVersionDifferential = this.vendorVersionDifferentialDAO.findByVendorVersion(
				Long.parseLong(vendorVersion.get(0)), Long.parseLong(vendorVersion.get(1)));

		if (vendorVersionDifferential != null
				&& vendorVersionDifferential.getVendorId() == Long.parseLong(vendorVersion.get(0))) {

			inventoryResponse = new InventoryResponse();

			inventoryResponse.setVendorId(vendorVersionDifferential.getVendorId());
			inventoryResponse.setCurrentDataVersionId(vendorVersionDifferential.getVersionId());
			inventoryResponse.setNewDataVersionId(vendorVersionDifferential.getLastSyncedVersionId());

			Query query = session
					.createSQLQuery(
							"select vim.* " + " from vendor_item_master vim where item_code in (:itemCodes) "
									+ " order by vim.item_code ")
					.addEntity("vendor_item_master", VendorItemMaster.class)
					.setParameter("itemCodes", convertStringToTokens(vendorVersionDifferential.getDeltaItemCodes()));

			List<VendorItemMaster> vendorItemMasterList = query.list();
			System.out.println(vendorItemMasterList.size() + " rows found after executing query:"
					+ query.getQueryString());

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

	private void setupInitialCache() {

		List<VendorVersionDifferential> vendorVersionDifferentials = this.vendorVersionDifferentialDAO.findAll();

		for (VendorVersionDifferential diffInstance : vendorVersionDifferentials) {
			try {
				load(diffInstance.getVendorId() + "-" + diffInstance.getVersionId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
