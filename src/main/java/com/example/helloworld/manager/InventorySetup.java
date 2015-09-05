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
 * 
 * Intention of this class is to run as a background thread and continue to look
 * for new arriving inventory. Once something is found, make the updates to the
 * required data sets used for serving. Note that, inventory_master stores all
 * the incoming inventory data. From there it gets copied to product_master,
 * vendor_item_master and vendor_item_differential.
 * 
 * inventory_master (Global inventory of all time) product_master (Global
 * product data, vendor specific) vendor_item_master (Current latest vendor
 * specific inventory)
 * 
 */

public class InventorySetup implements Runnable {

	private SessionFactory sessionFactory;

	public InventorySetup(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
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

				Session session = sessionFactory.openSession();

				// Look for all new data in inventory_master w.r.t.
				// vendor_item_master
				Query query = session
						.createSQLQuery(
								"select im.* "
										+ " from inventory_master im inner join "
										+ " 	(select barcode, vendor_id, max(version_id) version_id from inventory_master group by barcode, vendor_id) in_inner "
										+ " on im.barcode = in_inner.barcode "
										+ " and im.version_id = in_inner.version_id "
										+ " and im.vendor_id = in_inner.vendor_id " + " and im.version_id > "
										+ "		(select coalesce (max(version_id), 0) " + "		from vendor_item_master "
										+ "		where barcode = im.barcode " + "		and vendor_id = im.vendor_id) "
										+ " order by im.vendor_id")
						.addEntity("inventory_master", InventoryMaster.class);

				List<InventoryMaster> inventoryMasterList = query.list();
				System.out.println(inventoryMasterList.size() + " rows found after executing query:"
						+ query.getQueryString());

				// Did we find anything new?
				for (InventoryMaster instance : inventoryMasterList) {

					InventoryMaster imData = (InventoryMaster) instance;

					Long vendorId = imData.getVendorId();
					Long barcode = imData.getBarcode();
					Long productId;

					System.out.println("Looking for product data on barcode:" + imData.getBarcode());

					// First see if the product for this barcode exists already
					Query pmQuery = session
							.createSQLQuery("select pm.* from product_master pm where barcode = :barcode")
							.addEntity("product_master", ProductMaster.class).setParameter("barcode", barcode);

					List<ProductMaster> productMasterList = pmQuery.list();

					ProductVendorMap pvMap = null;
					ProductMaster productMasterExisting = null;

					// Setup a new product_master with latest data, that may be
					// same as existing
					ProductMaster productMasterNew = new ProductMaster();
					productMasterNew.setBarcode(imData.getBarcode());
					productMasterNew.setDescription(imData.getDescription());
					productMasterNew.setImageJSON(imData.getImageJSON());
					productMasterNew.setName(imData.getName());
					productMasterNew.setTagLine(imData.getTagLine());
					productMasterNew.setVendorId(imData.getVendorId().toString());

					session.getTransaction().begin();

					// If this product doesn't already exits in product_master,
					// add it
					if (productMasterList == null || productMasterList.size() < 1) {
						session.save(productMasterNew);
						productId = productMasterNew.getId();
					} else {

						productMasterExisting = productMasterList.get(0);
						pvMap = new ProductVendorMap(productMasterExisting);
						productMasterNew.setId(productMasterExisting.getId());
						productId = productMasterNew.getId();
						productMasterNew.setVendorId(productMasterExisting.getVendorId());

						/*
						 * When product exist in the master, compare with latest
						 * data
						 */
						boolean existingNewProductAreSame = productMasterExisting.equals(productMasterNew);

						// Did this product existed in master with this vendor?
						if (pvMap.getVendorIds().contains(vendorId)) {

							if (!existingNewProductAreSame) {

								/*
								 * Product existed with this vendor, but some
								 * data about product has changes, now create
								 * new entry for this vendor, and remove him
								 * from existing product entry
								 */

								pvMap.removeVendor(vendorId);
								productMasterExisting.setVendorId(pvMap.getVendorsAsStringList());

								// TODO Need to understand this better
								session.saveOrUpdate(productMasterExisting);
								session.merge(productMasterExisting);

								productMasterNew.setId(0);
								productMasterNew.setVendorId(vendorId.toString());
								session.save(productMasterNew);
								productId = productMasterNew.getId();

							} else {
								/*
								 * Everything exists, product details are same,
								 * and already associated with this vendor. Do
								 * nothing.
								 */
							}

						} else {

							if (existingNewProductAreSame) {

								/*
								 * Product existed already, no data changes,
								 * simply add this vendor to the list
								 */
								productMasterExisting.setVendorId(productMasterExisting.getVendorId() + "," + vendorId);
								session.saveOrUpdate(productMasterExisting);
								session.merge(productMasterExisting);
							} else {

								/*
								 * Product data is different and this product
								 * never existed for this vendor, create new
								 * vendor specific product entry
								 */
								productMasterNew.setId(0);
								productMasterNew.setVendorId(vendorId.toString());
								session.save(productMasterNew);
								productId = productMasterNew.getId();
							}
						}
					}

					// Look for latest vendor data for this barcode
					Query vimQuery = session
							.createSQLQuery(
									"select vim.* from vendor_item_master vim where barcode = :barcode and vendor_id = :vendorId")
							.addEntity("vendor_item_master", VendorItemMaster.class).setParameter("barcode", barcode)
							.setParameter("vendorId", vendorId);

					boolean vendorRecordAlreadyExist = false;
					List<VendorItemMaster> vendorItemMasterList = vimQuery.list();

					VendorItemMaster vendorItemMasterExisting = null;

					if (vendorItemMasterList != null && vendorItemMasterList.size() > 0) {
						vendorRecordAlreadyExist = true;
						vendorItemMasterExisting = vendorItemMasterList.get(0);
					}

					/*
					 * If the vendor record already existed and still got picked
					 * for processing, means something has changed. Simply pull
					 * all data changes in and update.
					 * 
					 * Very unlikely case of where only product information has
					 * changed. Hence ignoring that for now. Keeping a TODO if
					 * ever need to optimize on that.
					 */
					if (vendorRecordAlreadyExist) {

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

					// Check for new inventory every 'X' seconds.
					Thread.sleep(6000);

				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}
