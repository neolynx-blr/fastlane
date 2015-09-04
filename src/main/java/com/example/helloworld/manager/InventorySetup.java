package com.example.helloworld.manager;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.example.helloworld.core.InventoryMaster;
import com.example.helloworld.core.ProductMaster;
import com.example.helloworld.core.ProductVendorMap;
import com.example.helloworld.core.VendorItemMaster;

/**
 * Created by nitesh.garg on 04-Sep-2015
 */
public class InventorySetup implements Runnable {
	
	private SessionFactory sessionFactory;
	
	public InventorySetup(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {

		while (true) {
			
			try {
				Thread.sleep(6000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			Session session = sessionFactory.openSession();
			Query query = session
					.createSQLQuery(
							"select im.* "
									+ " from inventory_master im inner join "
									+ " 	(select barcode, vendor_id, max(version_id) version_id from inventory_master group by barcode, vendor_id) in_inner "
									+ " on im.barcode = in_inner.barcode "
									+ " and im.version_id = in_inner.version_id "
									+ " and im.vendor_id = in_inner.vendor_id "
									+ " and im.version_id > "
									+ "		(select coalesce (max(version_id), 0) "
									+ "		from vendor_item_master "
									+ "		where barcode = im.barcode "
									+ "		and vendor_id = im.vendor_id) "
									+ " order by im.vendor_id")
					.addEntity("inventory_master", InventoryMaster.class);
			
			List<InventoryMaster> inventoryMasterList = query.list();
			System.out.println(inventoryMasterList.size() + " rows found after executing query:" + query.getQueryString());
			
			for (InventoryMaster instance : inventoryMasterList) {
				
				InventoryMaster imData = (InventoryMaster) instance;
				
				Long vendorId = imData.getVendorId();
				Long barcode = imData.getBarcode();
				Long productId;
				
				System.out.println("Looking for product data on barcode:" + imData.getBarcode());

				Query pmQuery = session
						.createSQLQuery("select pm.* from product_master pm where barcode = :barcode")
						.addEntity("product_master", ProductMaster.class)
						.setParameter("barcode", barcode);
				
				List<ProductMaster> productMasterList = pmQuery.list();

				ProductVendorMap pvMap = null;
				ProductMaster productMasterExisting = null;
				
				ProductMaster productMasterNew = new ProductMaster();
				productMasterNew.setBarcode(imData.getBarcode());
				productMasterNew.setDescription(imData.getDescription());
				productMasterNew.setImageJSON(imData.getImageJSON());
				productMasterNew.setName(imData.getName());
				productMasterNew.setTagLine(imData.getTagLine());
				productMasterNew.setVendorId(imData.getVendorId().toString());

				session.getTransaction().begin();
				if(productMasterList == null || productMasterList.size() < 1) {
					session.save(productMasterNew);	
					productId = productMasterNew.getId();
				} else {
					
					productMasterExisting = productMasterList.get(0);
					pvMap = new ProductVendorMap(productMasterExisting);
					productMasterNew.setId(productMasterExisting.getId());
					productId = productMasterNew.getId();
					productMasterNew.setVendorId(productMasterExisting.getVendorId());
					
					boolean existingNewProductAreSame = productMasterExisting.equals(productMasterNew);
					
					if(pvMap.getVendorIds().contains(vendorId)) {
						if(!existingNewProductAreSame) {
							
							productMasterExisting.setDescription(productMasterNew.getDescription());
							productMasterExisting.setImageJSON(productMasterNew.getImageJSON());
							productMasterExisting.setName(productMasterNew.getName());
							productMasterExisting.setTagLine(productMasterNew.getTagLine());
							
							session.saveOrUpdate(productMasterExisting);
							session.merge(productMasterExisting);
						}
					} else {
						if(existingNewProductAreSame) {
							productMasterExisting.setVendorId(productMasterExisting.getVendorId()+","+vendorId);
							session.saveOrUpdate(productMasterExisting);
							session.merge(productMasterExisting);
						} else {
							productMasterNew.setId(0);
							productMasterNew.setVendorId(vendorId.toString());
							session.save(productMasterNew);
							productId = productMasterNew.getId();
						}
					}
				}
				
				Query vimQuery = session
						.createSQLQuery("select vim.* from vendor_item_master vim where barcode = :barcode and vendor_id = :vendorId")
						.addEntity("vendor_item_master", VendorItemMaster.class)
						.setParameter("barcode", barcode)
						.setParameter("vendorId", vendorId);
				
				boolean vendorRecordAlreadyExist = false;
				List<VendorItemMaster> vendorItemMasterList = vimQuery.list();

				VendorItemMaster vendorItemMasterExisting = null;
				
				if(vendorItemMasterList != null && vendorItemMasterList.size() > 0) {
					vendorRecordAlreadyExist = true;
					vendorItemMasterExisting = vendorItemMasterList.get(0); 
				}
				
				if(vendorRecordAlreadyExist) {
					
					vendorItemMasterExisting.setDiscountType(imData.getDiscountType());
					vendorItemMasterExisting.setDiscountValue(imData.getDiscountValue());
					vendorItemMasterExisting.setImageJSON(imData.getImageJSON());
					vendorItemMasterExisting.setItemCode(imData.getItemCode());
					vendorItemMasterExisting.setMrp(imData.getMrp());
					vendorItemMasterExisting.setPrice(imData.getPrice());
					vendorItemMasterExisting.setVersionId(imData.getVersionId());
					vendorItemMasterExisting.setCreatedOn(new java.sql.Date(System.currentTimeMillis()));
					
					session.saveOrUpdate(vendorItemMasterExisting);
					session.merge(vendorItemMasterExisting);
				} else {
					VendorItemMaster vendorItemMasterNew = new VendorItemMaster();
					vendorItemMasterNew.setVendorId(imData.getVendorId());
					vendorItemMasterNew.setItemCode(imData.getItemCode());
					vendorItemMasterNew.setProductId(productId);
					vendorItemMasterNew.setVersionId(imData.getVersionId());
					vendorItemMasterNew.setBarcode(imData.getBarcode());
					vendorItemMasterNew.setMrp(imData.getMrp());
					vendorItemMasterNew.setPrice(imData.getPrice());
					vendorItemMasterNew.setImageJSON(imData.getImageJSON());
					vendorItemMasterNew.setDiscountType(imData.getDiscountType());
					vendorItemMasterNew.setDiscountValue(imData.getDiscountValue());
					vendorItemMasterNew.setCreatedOn(new java.sql.Date(System.currentTimeMillis()));
					session.save(vendorItemMasterNew);
				}

				session.getTransaction().commit();
			}
			
		}

		
	}

}
