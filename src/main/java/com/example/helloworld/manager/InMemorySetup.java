package com.example.helloworld.manager;

import java.util.List;

import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.ItemResponse;
import com.example.helloworld.core.VendorItemMaster;
import com.example.helloworld.core.VendorVersionDifferential;
import com.example.helloworld.db.VendorVersionDifferentialDAO;
import com.google.common.cache.LoadingCache;

/**
 * Created by nitesh.garg on 06-Sep-2015
 */
public class InMemorySetup implements Runnable {

	private SessionFactory sessionFactory;
	private VendorVersionDifferentialDAO vendorVersionDifferentialDAO;

	LoadingCache<String, InventoryResponse> differentialInventoryCache;

	public InMemorySetup(SessionFactory sessionFactory, VendorVersionDifferentialDAO vendorVersionDifferentialDAO, 	LoadingCache<String, InventoryResponse> differentialInventoryCache) {
		super();
		this.sessionFactory = sessionFactory;
		this.vendorVersionDifferentialDAO = vendorVersionDifferentialDAO;
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
	public void run() {

		// Keeping running for ever in the background looking for new inventory
		while (true) {

			try {

				// Check for new inventory every 'X' seconds.
				Thread.sleep(10000);

				Session session = sessionFactory.openSession();

				List<VendorVersionDifferential> vendorVersionDifferentials = this.vendorVersionDifferentialDAO.findAll();

				for (VendorVersionDifferential diffInstance : vendorVersionDifferentials) {
					
					InventoryResponse inventoryResponse = new InventoryResponse();
					
					inventoryResponse.setVendorId(diffInstance.getVendorId());
					inventoryResponse.setCurrentDataVersionId(diffInstance.getVersionId());
					inventoryResponse.setNewDataVersionId(diffInstance.getLastSyncedVersionId());

					Query query = session
							.createSQLQuery(
									"select vim.* " + " from vendor_item_master vim where item_code in (:itemCodes) "
											+ " order by vim.item_code ")
							.addEntity("vendor_item_master", VendorItemMaster.class)
							.setParameter("itemCodes", convertStringToTokens(diffInstance.getDeltaItemCodes()));

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
					this.differentialInventoryCache.put(diffInstance.getVendorId()+"-"+diffInstance.getVersionId(), inventoryResponse);
				}

				session.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
