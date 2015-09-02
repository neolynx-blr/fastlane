package com.example.helloworld.manager;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.example.helloworld.core.AllInventory;
import com.example.helloworld.core.InventorySync;
import com.example.helloworld.core.ItemCore;
import com.example.helloworld.core.ItemDetail;
import com.example.helloworld.core.ProductCore;
import com.example.helloworld.db.AllInventoryDAO;
import com.example.helloworld.db.InventorySyncDAO;
import com.example.helloworld.db.ItemCoreDAO;
import com.example.helloworld.db.ItemDetailDAO;
import com.example.helloworld.db.ProductCoreDAO;
import com.example.helloworld.db.VendorDAO;

/**
 * Created by nitesh.garg on 28-Aug-2015
 */
public class InventoryCurator {

	private SessionFactory sessionFactory;
	private final VendorDAO vendorDAO;
	private final InventorySyncDAO inventorySyncDAO;
	private final AllInventoryDAO allInventoryDAO;
	private final ProductCoreDAO productCoreDAO;
	private final ItemCoreDAO itemCoreDAO;
	private final ItemDetailDAO itemDetailDAO;

	public InventoryCurator(VendorDAO vendorDAO,
			InventorySyncDAO inventorySyncDAO, AllInventoryDAO allInventoryDAO,
			SessionFactory sessionFactory, ProductCoreDAO productCoreDAO, ItemCoreDAO itemCoreDAO, ItemDetailDAO itemDetailDAO) {
		super();
		this.vendorDAO = vendorDAO;
		this.inventorySyncDAO = inventorySyncDAO;
		this.allInventoryDAO = allInventoryDAO;
		this.sessionFactory = sessionFactory;
		this.productCoreDAO = productCoreDAO;
		this.itemCoreDAO = itemCoreDAO;
		this.itemDetailDAO = itemDetailDAO;
	}

	public void prepareInventory() {

		Long currentTime = System.currentTimeMillis();
		List<InventorySync> inventoryList = this.inventorySyncDAO.findAll();

		for (InventorySync instance : inventoryList) {

			Long vendorId = instance.getVendorId();

			System.out.println("Processing Vendor:" + vendorId);
			List<AllInventory> vendorInventoryList = this.allInventoryDAO
					.getRecentInventoryUpdatesByVendor(vendorId,
							instance.getLastSyncedVersionId());
			
			for(AllInventory allInventory : vendorInventoryList) {
				System.out.println("Data::"+allInventory.toString());
			}

			Session session = this.sessionFactory.getCurrentSession();

			List<ItemCore> existingICq = this.itemCoreDAO.findAll();
			for(ItemCore ic : existingICq) {
				System.out.println("IC-Data::" + ic.toString());
			}
			
			List<ProductCore> existingPCq = this.productCoreDAO.findAll();
			for(ProductCore ic : existingPCq) {
				System.out.println("PC-Data::" + ic.toString());
			}
			
			for (AllInventory instance2 : vendorInventoryList) {
				Query queryOld = session
						.createQuery("select count(*) from ItemCore p where vendorId = :vendorId and productId = (select id from ProductCore where barcode = :barcode)");
				queryOld.setLong("vendorId", vendorId);
				queryOld.setLong("barcode", instance2.getBarcode());

				System.out.println("QueryOld::" + queryOld.getQueryString() + "," + vendorId + "," + instance2.getBarcode());
				//System.out.println("QueryOld::" + queryOld.getQueryString());
				//System.out.println("Old::" + queryOld.getFetchSize());
				
				List<Object[]> allRowsn = queryOld.list();
				System.out.println("OldGoal::"+allRowsn.size());

				if (allRowsn.size() > 0) {
					
					System.out.println("Need to update existing record");
					ProductCore existingPC = this.productCoreDAO.findByBarcodeId(instance2.getBarcode());
					ItemCore existingIC = this.itemCoreDAO.findByVendorProduct(vendorId, existingPC.getId());
					ItemDetail existingID = this.itemDetailDAO.findByItemCode(existingIC.getId());
					
					
					
					
					
				} else {
					
					System.out.println("Inserting New Record");
					ProductCore productCore = new ProductCore();
					productCore.setBarcode(instance2.getBarcode());
					productCore.setDescription(instance2.getDescription());
					productCore.setName(instance2.getName());
					productCore.setTagLine(instance2.getTagLine());
					productCore.setVendorId(vendorId);
					
					ItemCore itemCore = new ItemCore();
					itemCore.setVendorId(vendorId);
					
					ItemDetail itemDetail = new ItemDetail();
					itemDetail.setDiscountType(instance2.getDiscountType());
					itemDetail.setDiscountValue(instance2.getDiscountValue());
					itemDetail.setImageJSON(instance2.getImageJSON());
					itemDetail.setMrp(instance2.getMrp());
					itemDetail.setPrice(instance2.getPrice());
					itemDetail.setVendorId(vendorId);
					itemDetail.setVersionId(instance2.getVersionId());
					
					productCore = this.productCoreDAO.create(productCore);
					itemCore.setProductId(productCore.getId());

					itemCore = this.itemCoreDAO.create(itemCore);
					
					itemDetail.setItemId(Long.toString(itemCore.getId()));
					this.itemDetailDAO.create(itemDetail);
					
				}

			}

		}

	}

}
