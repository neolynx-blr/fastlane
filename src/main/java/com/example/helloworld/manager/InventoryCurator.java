package com.example.helloworld.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.helloworld.core.InventoryMaster;
import com.example.helloworld.core.ProductMaster;
import com.example.helloworld.core.ProductVendorMap;
import com.example.helloworld.core.VendorItemHistory;
import com.example.helloworld.core.VendorItemMaster;
import com.example.helloworld.core.VendorVersionDetail;
import com.example.helloworld.core.VendorVersionDifferential;
import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.InventoryResponse;

/**
 * Meant for curating any newly uploaded inventory into the master tables from
 * which the differentials are calculated and served
 * 
 * 
 * Created by nitesh.garg on 06-Sep-2015
 */

public class InventoryCurator {

	private final SessionFactory sessionFactory;
	static Logger LOGGER = LoggerFactory.getLogger(InventoryCurator.class);

	public InventoryCurator(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	/*
	 * Looks at inventory_master table and accordingly converts the new data
	 * into product and vendor-item master tables.
	 */
	@SuppressWarnings("unchecked")
	public void processNewInventory() {

		Session session = this.sessionFactory.openSession();

		/*
		 * Look for all new data in inventory_master w.r.t. vendor_item_master,
		 * i.e. compared on version_id, pick all new data rows which were never
		 * pushed to vendor_item_master.
		 */
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

		int updatedInventorySize = 0;
		int newInventorySize = inventoryMasterList.size();

		LOGGER.info("[{}] new entries found in the inventory_master since the last update of data.",
				inventoryMasterList.size());
		LOGGER.debug("Query Executed::\n{}", query.getQueryString());

		// Did we find anything new?
		for (InventoryMaster instance : inventoryMasterList) {

			InventoryMaster imData = (InventoryMaster) instance;

			VendorItemHistory vendorItemHistory;
			VendorItemMaster vendorItemMasterNew = new VendorItemMaster();

			Long productId = 0L;
			Long barcode = imData.getBarcode();
			Long vendorId = imData.getVendorId();

			LOGGER.debug("Working with data Vendor:Barcode:Version {}:{}:{}", vendorId, barcode, imData.getVersionId());

			// First see if the product(s) for this bar-code exists already
			Query pmQuery = session.createSQLQuery("select pm.* from product_master pm where barcode = :barcode ") // and
																													// vendor_id
																													// ilike
																													// :vendorId")
					.addEntity("product_master", ProductMaster.class).setParameter("barcode", barcode);
			// .setParameter("vendorId", "%" + vendorId.toString() + "%");

			List<ProductMaster> productMasterList = pmQuery.list();

			ProductVendorMap pvMap = null;

			/*
			 * Setup a new product_master with latest data, that may be same as
			 * existing
			 */
			ProductMaster productMasterNew = new ProductMaster();
			productMasterNew.setBarcode(imData.getBarcode());
			productMasterNew.setDescription(imData.getDescription());
			productMasterNew.setImageJSON(imData.getImageJSON());
			productMasterNew.setName(imData.getName());
			productMasterNew.setTagLine(imData.getTagLine());
			productMasterNew.setVendorId(imData.getVendorId().toString());

			/*
			 * To have lesser processing inside the transaction, pulling the
			 * vendor-item data also from DB before the transaction begins. Note
			 * that this will definitely be required later because a new entry
			 * has been found in inventory_master already.
			 * 
			 * Look for latest vendor data for this barcode.
			 */
			Query vimQuery = session
					.createSQLQuery(
							"select vim.* from vendor_item_master vim where barcode = :barcode and vendor_id = :vendorId")
					.addEntity("vendor_item_master", VendorItemMaster.class).setParameter("barcode", barcode)
					.setParameter("vendorId", vendorId);

			boolean vendorRecordAlreadyExist = false;
			VendorItemMaster vendorItemMasterExisting = null;

			List<VendorItemMaster> vendorItemMasterList = vimQuery.list();

			if (CollectionUtils.isNotEmpty(vendorItemMasterList)) {
				vendorRecordAlreadyExist = true;
				vendorItemMasterExisting = vendorItemMasterList.get(0);
				LOGGER.debug("Found existing Vendor-Barcode-Version record [{}-{}-{}]",
						vendorItemMasterExisting.getVendorId(), vendorItemMasterExisting.getBarcode(),
						vendorItemMasterExisting.getVersionId());
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

			boolean shouldNewProductBeAdded = true;
			session.getTransaction().begin();

			/*
			 * For all products existing in the system with same barcode
			 */
			for (ProductMaster productMasterExisting : productMasterList) {

				pvMap = new ProductVendorMap(productMasterExisting);
				productMasterNew.setVendorId(productMasterExisting.getVendorId());

				/*
				 * When product exist in the master, compare with latest data
				 */
				int existingNewProductAreSame = productMasterExisting.compareTo(productMasterNew);

				// Did this product-vendor mapping previously existed?
				LOGGER.debug("Is there a change in product information? Answer [{}]",
						existingNewProductAreSame == 0 ? "True" : "False");
				LOGGER.debug(
						"Set of vendors for which the product previously existed are [{}], and is currently processed vendor [{}] present?",
						pvMap.getVendorsAsStringList(), vendorId, pvMap.getVendorIds().contains(vendorId));

				if (pvMap.getVendorIds().contains(vendorId)) {

					if (existingNewProductAreSame != 0) {

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

							shouldNewProductBeAdded = false;
							productId = productMasterExisting.getId();

							LOGGER.debug("Completed updating the details of product [{}] for vendor [{}]",
									productMasterExisting.getName(), productMasterExisting.getVendorId());

						} else {

							/*
							 * If product information has changed only for this
							 * vendor, remove him from previous list and create
							 * it's own entry.
							 */
							pvMap.removeVendor(vendorId);
							productMasterExisting.setVendorId(pvMap.getVendorsAsStringList());

							session.saveOrUpdate(productMasterExisting);
							session.merge(productMasterExisting);

							LOGGER.debug(
									"Will created new product [{}] for vendor [{}] while removing this vendor from the older list.",
									productMasterNew.getName(), productMasterNew.getVendorId());
						}

					} else {
						/*
						 * Everything exists, product details are same, and
						 * already associated with this vendor. Do nothing.
						 */
						shouldNewProductBeAdded = false;
					}

				} else {

					if (existingNewProductAreSame == 0) {

						/*
						 * Product existed already, no data changes, simply add
						 * this vendor to the list
						 */
						productMasterExisting.setVendorId(productMasterExisting.getVendorId() + "," + vendorId);
						session.saveOrUpdate(productMasterExisting);
						session.merge(productMasterExisting);

						shouldNewProductBeAdded = false;
						productId = productMasterExisting.getId();

						LOGGER.debug(
								"Nothing changed for the product [{}] except that the vendor [{}] got added to it",
								productMasterExisting.getName(), vendorId);

					} else {
						/*
						 * Product data is different and this product never
						 * existed for this vendor, so very likely a new vendor
						 * specific product entry is to be created. But wait for
						 * that till all products with same barcode are
						 * iterated.
						 */
					}
				}
			}

			if (shouldNewProductBeAdded) {
				productMasterNew.setVendorId(vendorId.toString());
				session.save(productMasterNew);
				productId = productMasterNew.getId();

				LOGGER.debug("Created new product [{}] for vendor [{}].", productMasterNew.getName(),
						productMasterNew.getVendorId());
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

			updatedInventorySize++;
			session.getTransaction().commit();
		}

		session.clear();
		session.close();
		LOGGER.info(
				"Completed processing new inventory where [{}] were identfied to be changed, and [{}] got updated successfully.",
				newInventorySize, updatedInventorySize);

	}

	/*
	 * This function looks at any new vendor specific inventory added and
	 * accordingly updates the caches.
	 */
	@SuppressWarnings("unchecked")
	public void processVendorVersionMetaOld(LoadingCache<String, InventoryResponse> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache) {

		boolean vendorDataUpdated = false;
		Session session = this.sessionFactory.openSession();

		// Get the latest known version for each vendor
		Query vendorItemMasterQuery = session
				.createSQLQuery("select vendor_id, max(version_id) max_version_id from vendor_item_master group by vendor_id");

		List<Object[]> vimDataRows = vendorItemMasterQuery.list();
		LOGGER.debug("[{}] vendors selected from vendor-item-master tables to be checked for any inventory updates.",
				vimDataRows.size());

		// For every known vendor and it's max version-id
		for (Object[] vimDataRow : vimDataRows) {

			Long vendorId = (Long) Long.parseLong(vimDataRow[0].toString());
			Long maxVersionId = (Long) Long.parseLong(vimDataRow[1].toString());

			// Look for the version details and their differentials
			Query vendorItemCurrentStateQuery = session
					.createSQLQuery(
							"select {vvdf.*}, {vvd.*} "
									+ " from vendor_version_differential vvdf, vendor_version_detail vvd "
									+ " where vvd.vendor_id = vvdf.vendor_id and vvd.vendor_id = :vendorId ")
					.addEntity("vvdf", VendorVersionDifferential.class).addEntity("vvd", VendorVersionDetail.class)
					.setParameter("vendorId", vendorId);

			List<Object[]> vvdDataRows = vendorItemCurrentStateQuery.list();
			LOGGER.debug(
					"Looked at vendor version details along with differential data and found [{}] rows for processing against the latest vendor-item-master table",
					vvdDataRows.size());

			VendorVersionDifferential vendorVersionDifferentialNew = new VendorVersionDifferential();
			vendorVersionDifferentialNew.setLastModifiedOn(new java.sql.Date(System.currentTimeMillis()));
			vendorVersionDifferentialNew.setLastSyncedVersionId(maxVersionId);
			vendorVersionDifferentialNew.setVendorId(vendorId);
			vendorVersionDifferentialNew.setVersionId(maxVersionId);

			if (CollectionUtils.isNotEmpty(vvdDataRows)) {

				// For every know vendor-version detail data
				for (Object[] vvdDataRow : vvdDataRows) {

					VendorVersionDetail vendorVersionDetail = (VendorVersionDetail) vvdDataRow[1];
					VendorVersionDifferential vendorVersionDifferential = (VendorVersionDifferential) vvdDataRow[0];

					/*
					 * If the latest known version in item-master is more than
					 * the previously synced version in detail/differential
					 * data, need updates to those tables.
					 */
					if (vendorVersionDetail.getLatestSyncedVersionId() < maxVersionId) {

						Long lastSyncedVersionId = vendorVersionDetail.getLatestSyncedVersionId();

						LOGGER.debug(
								"For vendor [{}], the last synced version [{}] is older than latest version [{}] found in vendor-item-master table.",
								vendorId, lastSyncedVersionId, maxVersionId);

						vendorVersionDetail.setLatestSyncedVersionId(maxVersionId);
						vendorVersionDetail.setValidVersionIds(vendorVersionDetail.getValidVersionIds() + ","
								+ maxVersionId);
						vendorVersionDetail.setLastModifiedOn(new java.sql.Date(System.currentTimeMillis()));

						/*
						 * Among all the new data for this vendor under
						 * processing, group the new items by various new
						 * version ids.
						 */
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

						LOGGER.debug("[{}] different new versions found for vendor [{}] since the last sync up.");

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

			} else {

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

				LOGGER.info(
						"Vendor [{}] is seen for the first time and added to it's version details and differential tables using verision [{}]",
						vendorId, maxVersionId);
				vendorDataUpdated = true;

			}

			if (vendorDataUpdated) {
				System.out.println("Found some vendor {" + vendorId + "} data update, will call for cache refreshes.");
				// TODO : What if this fails? No backup for this one
				vendorVersionCache.refresh(vendorId);
				differentialInventoryCache.refresh(vendorId + "-" + vendorVersionCache.getIfPresent(vendorId));
			}

		}

	}

	/*
	 * This function looks at any new vendor specific inventory added and
	 * accordingly updates the caches.
	 */
	@SuppressWarnings("unchecked")
	public void processVendorVersionMeta(LoadingCache<String, InventoryResponse> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache) {

		Session session = this.sessionFactory.openSession();

		/*
		 * Get the vendor-version combinations where latest known version hasn't
		 * been synced
		 */
		Query vendorVersionDetailQuery = session
				.createSQLQuery(
						"select vvd.* from vendor_version_detail vvd where vvd.latest_synced_version_id < (select max(version_id) from vendor_item_master vim where vendor_id = vvd.vendor_id)")
				.addEntity("vendor_version_detail", VendorVersionDetail.class);

		List<VendorVersionDetail> vvdDataRows = vendorVersionDetailQuery.list();
		LOGGER.debug(
				"[{}] vendors selected from vendor-version-details tables to be synced up with the latest version.",
				vvdDataRows.size());

		// For every vendor where sync in required
		for (VendorVersionDetail vvdDataRow : vvdDataRows) {

			Long vendorId = vvdDataRow.getVendorId();
			Long lastSyncedVersionId = vvdDataRow.getLatestSyncedVersionId();

			Query maxVendorIdQuery = session.createSQLQuery(
					" select max(version_id) maxVersionId from vendor_item_master vim where vendor_id = :vendorId ")
					.setParameter("vendorId", vendorId);

			Long maxVersionId = Long.parseLong(maxVendorIdQuery.list().get(0).toString());
			LOGGER.info("Working on vendor [{}] whose last and current version ids are [{}] and [{}]", vendorId,
					vvdDataRow.getLatestSyncedVersionId(), maxVersionId);

			// Update the Vendor Version details for updates at some point
			vvdDataRow.setLatestSyncedVersionId(maxVersionId);
			vvdDataRow.setLastModifiedOn(new java.sql.Date(System.currentTimeMillis()));
			vvdDataRow.setValidVersionIds((StringUtils.isEmpty(vvdDataRow.getValidVersionIds()) ? maxVersionId
					.toString() : vvdDataRow.getValidVersionIds() + "," + maxVersionId));

			// Look for all the version details and their differentials
			Query vendorVersionDiffQuery = session
					.createSQLQuery(
							"select vvdf.* from vendor_version_differential vvdf where vvdf.vendor_id = :vendorId ")
					.addEntity("vvdf", VendorVersionDifferential.class).setParameter("vendorId", vendorId);

			List<VendorVersionDifferential> vvdfDataRows = vendorVersionDiffQuery.list();
			LOGGER.debug("Will be iterating over [{}] differential versions knows for vendor [{}] and update",
					vvdfDataRows.size(), vendorId);

			// Given the new version, create entry for that for sure
			VendorVersionDifferential vendorVersionDifferentialNew = new VendorVersionDifferential();
			vendorVersionDifferentialNew.setLastModifiedOn(new java.sql.Date(System.currentTimeMillis()));
			vendorVersionDifferentialNew.setLastSyncedVersionId(maxVersionId);
			vendorVersionDifferentialNew.setVendorId(vendorId);
			vendorVersionDifferentialNew.setVersionId(maxVersionId);

			if (CollectionUtils.isEmpty(vvdfDataRows)) {

				/*
				 * If no row exist already containing differential data, means
				 * its the first time vendor differentials are being recorded.
				 * So, simply add up the new row and save change on
				 * vendor-version-detail
				 */
				session.getTransaction().begin();

				session.saveOrUpdate(vvdDataRow);
				session.merge(vvdDataRow);

				session.save(vendorVersionDifferentialNew);

				session.getTransaction().commit();
				LOGGER.info(
						"Noticed first time differentials added for vendor-version [{}-{}]. Completed successfully.",
						vendorId, maxVersionId);

			} else {

				/*
				 * Otherwise, for given vendor, pick all the updated item codes
				 * that have changed since the last sync version and compare and
				 * update those with all differential rows.
				 */
				Query vendorItemDifferentialQuery = session
						.createSQLQuery(
								" select string_agg(item_code, ',') from vendor_item_master vim where vim.vendor_id = :vendorId and vim.version_id > :lastSyncedVersionId and vim.version_id <= :latestVersionId group by vendor_id ")
						.setParameter("vendorId", vendorId).setParameter("lastSyncedVersionId", lastSyncedVersionId)
						.setParameter("latestVersionId", maxVersionId);

				String newItemDifferential = (String) vendorItemDifferentialQuery.list().get(0);
				// String newItemDifferential = diffDataRows[0];

				LOGGER.debug("[{}] are the newly updated item codes found for vendor [{}] since the last sync up.",
						newItemDifferential, vendorId);

				session.getTransaction().begin();

				/*
				 * For every row in the differential data, add the new item
				 * codes and persist
				 */
				for (VendorVersionDifferential vvdfDataRow : vvdfDataRows) {

					String existingItemDifferential = vvdfDataRow.getDeltaItemCodes();

					vvdfDataRow.setLastSyncedVersionId(maxVersionId);
					vvdfDataRow.setLastModifiedOn(new java.sql.Date(System.currentTimeMillis()));

					if (StringUtils.isNotEmpty(newItemDifferential)) {

						if (StringUtils.isEmpty(existingItemDifferential)) {
							vvdfDataRow.setDeltaItemCodes(newItemDifferential);
						} else {

							Set<String> newDeltaItemCodes = new HashSet<String>();

							List<String> newStringTokens = new StrTokenizer(newItemDifferential, ",").getTokenList();
							List<String> existingStringTokens = new StrTokenizer(existingItemDifferential, ",")
									.getTokenList();

							newDeltaItemCodes.addAll(newStringTokens);
							newDeltaItemCodes.addAll(existingStringTokens);

							vvdfDataRow.setDeltaItemCodes(StringUtils.join(newDeltaItemCodes, ","));

						}

						session.saveOrUpdate(vvdfDataRow);
						session.merge(vvdfDataRow);

						LOGGER.debug("Updated version [{}] of vendor [{}] with item-codes [{}]",
								vvdfDataRow.getVersionId(), vvdfDataRow.getVendorId(), vvdfDataRow.getDeltaItemCodes());
					}
				}

				// Update the vendor-version detail first
				session.saveOrUpdate(vvdDataRow);
				session.merge(vvdDataRow);

				// Add the row for latest version as well
				session.save(vendorVersionDifferentialNew);
				session.getTransaction().commit();

			}

			LOGGER.info("Refreshing the cache entry for vendor [{}]", vendorId);
			// TODO : What if this fails? No backup for this one
			vendorVersionCache.refresh(vendorId);
			differentialInventoryCache.refresh(vendorId + "-" + vendorVersionCache.getIfPresent(vendorId));

		}

	}
}
