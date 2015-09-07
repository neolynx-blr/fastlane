package com.example.helloworld.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.example.helloworld.core.InventoryMaster;
import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.ProductMaster;
import com.example.helloworld.core.ProductVendorMap;
import com.example.helloworld.core.VendorItemHistory;
import com.example.helloworld.core.VendorItemMaster;
import com.example.helloworld.core.VendorVersionDetail;
import com.example.helloworld.core.VendorVersionDifferential;
import com.google.common.cache.LoadingCache;

/**
 * Created by nitesh.garg on 06-Sep-2015
 */

public class InventoryCurator {

	private final SessionFactory sessionFactory;

	public InventoryCurator(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	public void processNewInventory() {

		Session session = this.sessionFactory.openSession();

		// Look for all new data in inventory_master w.r.t.
		// vendor_item_master
		Query query = session
				.createSQLQuery(
						"select im.* "
								+ " from inventory_master im inner join "
								+ " 	(select barcode, vendor_id, max(version_id) version_id from inventory_master group by barcode, vendor_id) in_inner "
								+ " on im.barcode = in_inner.barcode " + " and im.version_id = in_inner.version_id "
								+ " and im.vendor_id = in_inner.vendor_id " + " and im.version_id > "
								+ "		(select coalesce (max(version_id), 0) " + "		from vendor_item_master "
								+ "		where barcode = im.barcode " + "		and vendor_id = im.vendor_id) "
								+ " order by im.vendor_id").addEntity("inventory_master", InventoryMaster.class);

		List<InventoryMaster> inventoryMasterList = query.list();
		System.out.println(inventoryMasterList.size() + " rows found after executing query:" + query.getQueryString());

		// Did we find anything new?
		for (InventoryMaster instance : inventoryMasterList) {

			InventoryMaster imData = (InventoryMaster) instance;

			VendorItemHistory vendorItemHistory;
			VendorItemMaster vendorItemMasterNew = new VendorItemMaster();

			Long vendorId = imData.getVendorId();
			Long barcode = imData.getBarcode();
			Long productId;

			System.out.println("Looking for product data on barcode:" + barcode + ", and vendor::" + vendorId);

			// First see if the product for this barcode exists already
			Query pmQuery = session
					.createSQLQuery(
							"select pm.* from product_master pm where barcode = :barcode and vendor_id ilike :vendorId")
					.addEntity("product_master", ProductMaster.class).setParameter("barcode", barcode)
					.setParameter("vendorId", "%" + vendorId.toString() + "%");

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

			/*
			 * To have lesser processing inside the transaction, pulling the
			 * vendor-item data also from DB before the transaction begins.
			 * 
			 * Look for latest vendor data for this barcode.
			 */
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
			 * If the vendor record already existed and still got picked for
			 * processing, means something has changed. Simply pull all data
			 * changes in and update.
			 * 
			 * Very unlikely case of where only product information has changed.
			 * Hence ignoring that for now. Keeping a TODO if ever need to
			 * optimize on that.
			 */
			if (vendorRecordAlreadyExist) {

				vendorItemMasterExisting.setDiscountType(imData.getDiscountType());
				vendorItemMasterExisting.setDiscountValue(imData.getDiscountValue());
				vendorItemMasterExisting.setImageJSON(imData.getImageJSON());
				vendorItemMasterExisting.setItemCode(imData.getItemCode());
				vendorItemMasterExisting.setMrp(imData.getMrp());
				vendorItemMasterExisting.setPrice(imData.getPrice());
				vendorItemMasterExisting.setVersionId(imData.getVersionId());
				vendorItemMasterExisting.setName(imData.getName());
				vendorItemMasterExisting.setTagLine(imData.getTagLine());
				vendorItemMasterExisting.setDescription(imData.getDescription());
				vendorItemMasterExisting.setCreatedOn(new java.sql.Date(System.currentTimeMillis()));

				vendorItemHistory = new VendorItemHistory(vendorItemMasterExisting);

			} else {

				vendorItemMasterNew.setVendorId(imData.getVendorId());
				vendorItemMasterNew.setItemCode(imData.getItemCode());
				vendorItemMasterNew.setVersionId(imData.getVersionId());
				vendorItemMasterNew.setBarcode(imData.getBarcode());
				vendorItemMasterNew.setMrp(imData.getMrp());
				vendorItemMasterNew.setPrice(imData.getPrice());
				vendorItemMasterNew.setImageJSON(imData.getImageJSON());
				vendorItemMasterNew.setDiscountType(imData.getDiscountType());
				vendorItemMasterNew.setDiscountValue(imData.getDiscountValue());
				vendorItemMasterNew.setName(imData.getName());
				vendorItemMasterNew.setTagLine(imData.getTagLine());
				vendorItemMasterNew.setDescription(imData.getDescription());
				vendorItemMasterNew.setCreatedOn(new java.sql.Date(System.currentTimeMillis()));

				vendorItemHistory = new VendorItemHistory(vendorItemMasterNew);

			}

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
				 * When product exist in the master, compare with latest data
				 */
				boolean existingNewProductAreSame = productMasterExisting.equals(productMasterNew);

				// Did this product existed in master with this vendor?
				System.out.println("VendorSet::" + pvMap.getVendorsAsStringList());
				if (pvMap.getVendorIds().contains(vendorId)) {

					if (!existingNewProductAreSame) {

						/*
						 * Product existed with this vendor, but some data about
						 * product has changes, now create new entry for this
						 * vendor, and remove him from existing product entry
						 */

						/*
						 * But what if the existing entry had only one vendor,
						 * the one currently being processed? In that case, no
						 * need to create
						 */
						if (pvMap.getVendorIds().size() == 1) {

							// Copy everything into existing, and update
							productMasterExisting.setDescription(imData.getDescription());
							productMasterExisting.setImageJSON(imData.getImageJSON());
							productMasterExisting.setName(imData.getName());
							productMasterExisting.setTagLine(imData.getTagLine());

							// TODO Need to understand this better
							session.saveOrUpdate(productMasterExisting);
							session.merge(productMasterExisting);

							productId = productMasterExisting.getId();

						} else {
							pvMap.removeVendor(vendorId);
							productMasterExisting.setVendorId(pvMap.getVendorsAsStringList());

							session.saveOrUpdate(productMasterExisting);
							session.merge(productMasterExisting);

							productMasterNew.setId(0);
							productMasterNew.setVendorId(vendorId.toString());
							session.save(productMasterNew);
							productId = productMasterNew.getId();
						}

					} else {
						/*
						 * Everything exists, product details are same, and
						 * already associated with this vendor. Do nothing.
						 */
					}

				} else {

					if (existingNewProductAreSame) {

						/*
						 * Product existed already, no data changes, simply add
						 * this vendor to the list
						 */
						productMasterExisting.setVendorId(productMasterExisting.getVendorId() + "," + vendorId);
						session.saveOrUpdate(productMasterExisting);
						session.merge(productMasterExisting);
					} else {

						/*
						 * Product data is different and this product never
						 * existed for this vendor, create new vendor specific
						 * product entry
						 */
						productMasterNew.setId(0);
						productMasterNew.setVendorId(vendorId.toString());
						session.save(productMasterNew);
						productId = productMasterNew.getId();
					}
				}
			}

			/*
			 * If the vendor record already existed and still got picked for
			 * processing, means something has changed. Simply pull all data
			 * changes in and update.
			 * 
			 * Very unlikely case of where only product information has changed.
			 * Hence ignoring that for now.
			 */
			if (vendorRecordAlreadyExist) {

				session.saveOrUpdate(vendorItemMasterExisting);
				session.merge(vendorItemMasterExisting);

				session.save(vendorItemHistory);

			} else {

				vendorItemMasterNew.setProductId(productId);
				vendorItemHistory.setProductId(productId);

				session.save(vendorItemMasterNew);
				session.save(vendorItemHistory);
			}

			session.getTransaction().commit();
		}

	}

	@SuppressWarnings("unchecked")
	public void processVendorVersionMeta(LoadingCache<String, InventoryResponse> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache) {

		Session session = this.sessionFactory.openSession();

		Query vendorItemMasterQuery = session
				.createSQLQuery("select vendor_id, max(version_id) max_version_id from vendor_item_master group by vendor_id");

		boolean vendorDataUpdated = false;
		List<Object[]> vimDataRows = vendorItemMasterQuery.list();
		System.out.println(vimDataRows.size() + " rows found after executing query:"
				+ vendorItemMasterQuery.getQueryString());

		for (Object[] vimDataRow : vimDataRows) {

			Long maxVersionId = (Long) Long.parseLong(vimDataRow[1].toString());
			Long vendorId = (Long) Long.parseLong(vimDataRow[0].toString());

			Query vendorItemCurrentStateQuery = session
					.createSQLQuery(
							"select {vvdf.*}, {vvd.*} "
									+ " from vendor_version_differential vvdf, vendor_version_detail vvd "
									+ " where vvd.vendor_id = vvdf.vendor_id and vvd.vendor_id = :vendorId ")
					.addEntity("vvdf", VendorVersionDifferential.class).addEntity("vvd", VendorVersionDetail.class)
					.setParameter("vendorId", vendorId);

			List<Object[]> vvdDataRows = vendorItemCurrentStateQuery.list();
			System.out.println(vvdDataRows.size() + " rows found after executing query:"
					+ vendorItemCurrentStateQuery.getQueryString());

			VendorVersionDifferential vendorVersionDifferentialNew = new VendorVersionDifferential();
			vendorVersionDifferentialNew.setLastModifiedOn(new java.sql.Date(System.currentTimeMillis()));
			vendorVersionDifferentialNew.setLastSyncedVersionId(maxVersionId);
			vendorVersionDifferentialNew.setVendorId(vendorId);
			vendorVersionDifferentialNew.setVersionId(maxVersionId);

			if (vvdDataRows == null || vvdDataRows.size() < 1) {

				// New vendor data, do the insertions
				VendorVersionDetail vendorVersionDetailNew = new VendorVersionDetail();

				vendorVersionDetailNew.setVendorId(vendorId);
				vendorVersionDetailNew.setLatestSyncedVersionId(maxVersionId);
				vendorVersionDetailNew.setValidVersionIds(maxVersionId.toString());
				vendorVersionDetailNew.setLastModifiedOn(new java.sql.Date(System.currentTimeMillis()));

				session.getTransaction().begin();
				session.save(vendorVersionDetailNew);
				session.save(vendorVersionDifferentialNew);
				session.getTransaction().commit();

				vendorDataUpdated = true;

			} else {

				for (Object[] vvdDataRow : vvdDataRows) {

					VendorVersionDetail vendorVersionDetail = (VendorVersionDetail) vvdDataRow[1];
					VendorVersionDifferential vendorVersionDifferential = (VendorVersionDifferential) vvdDataRow[0];

					if (vendorVersionDetail.getLatestSyncedVersionId() < maxVersionId) {

						Long lastSyncedVersionId = vendorVersionDetail.getLatestSyncedVersionId();

						vendorVersionDetail.setLatestSyncedVersionId(maxVersionId);
						vendorVersionDetail.setValidVersionIds(vendorVersionDetail.getValidVersionIds() + ","
								+ maxVersionId);
						vendorVersionDetail.setLastModifiedOn(new java.sql.Date(System.currentTimeMillis()));

						Query vendorItemDifferentialQuery = session
								.createSQLQuery(
										"select vendor_id, version_id, string_agg(item_code, ',') " + "from ("
												+ "	select vendor_id, version_id, item_code "
												+ "	from vendor_item_master vim " + " where vim.vendor_id = :vendorId "
												+ " and vim.version_id > :lastSyncedVersionId "
												+ " and vim.version_id <= :latestVersionId) vim_inner "
												+ "group by vendor_id, version_id order by vendor_id, version_id")
								.setParameter("vendorId", vendorId)
								.setParameter("lastSyncedVersionId", lastSyncedVersionId)
								.setParameter("latestVersionId", maxVersionId);

						Object[] diffDataRows = (Object[]) vendorItemDifferentialQuery.list().get(0);

						String newItemDifferential = (String) diffDataRows[2];
						String existingItemDifferential = vendorVersionDifferential.getDeltaItemCodes();

						vendorVersionDifferential.setLastModifiedOn(new java.sql.Date(System.currentTimeMillis()));
						vendorVersionDifferential.setLastSyncedVersionId(maxVersionId);

						if (newItemDifferential == null || newItemDifferential.trim().length() < 1) {
							// Very unlikely state. No operation
						} else {
							if (existingItemDifferential == null || existingItemDifferential.trim().length() < 1) {
								vendorVersionDifferential.setDeltaItemCodes(newItemDifferential);

							} else {

								Set<String> newDeltaItemCodes = new HashSet<String>();

								List<String> newStringTokens = new StrTokenizer(newItemDifferential, ",")
										.getTokenList();
								List<String> existingStringTokens = new StrTokenizer(existingItemDifferential, ",")
										.getTokenList();

								newDeltaItemCodes.addAll(newStringTokens);
								newDeltaItemCodes.addAll(existingStringTokens);

								StringBuffer updatedDeltaItemCodes = new StringBuffer();

								for (String itemCode : newDeltaItemCodes) {
									if (updatedDeltaItemCodes.length() < 1) {
										updatedDeltaItemCodes.append(itemCode);
									} else {
										updatedDeltaItemCodes.append("," + itemCode);
									}
								}

								vendorVersionDifferential.setDeltaItemCodes(updatedDeltaItemCodes.toString());

							}
							session.getTransaction().begin();

							session.saveOrUpdate(vendorVersionDifferential);
							session.merge(vendorVersionDifferential);

							session.save(vendorVersionDifferentialNew);

							session.getTransaction().commit();
							vendorDataUpdated = true;

						}

					} else {
						// Everything up-to-date, no-operation
						System.out.println("Turned out to be a no-op.");
					}

				}

			}

			if (vendorDataUpdated) {
				System.out.println("Found some vendor {" + vendorId + "} data update, will call for cache refreshes.");
				// TODO : What if this fails? No backup for this one
				vendorVersionCache.refresh(vendorId);
				differentialInventoryCache.refresh(vendorId + "-" + vendorVersionCache.getIfPresent(vendorId));
			}

		}

	}

}
