package com.neolynx.curator.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.client.InventoryInfo;
import com.neolynx.common.model.client.ItemInfo;
import com.neolynx.curator.core.InventoryMaster;
import com.neolynx.curator.core.ProductMaster;
import com.neolynx.curator.core.ProductVendorMap;
import com.neolynx.curator.core.VendorItemHistory;
import com.neolynx.curator.core.VendorItemMaster;
import com.neolynx.curator.core.VendorVersionDetail;
import com.neolynx.curator.core.VendorVersionDifferential;
import com.neolynx.curator.util.Constants;

/**
 * Meant to curate any newly uploaded inventory into the master tables from
 * which the independent vendor level tables are populated and then the current
 * inventory and differentials data cache are built for serving.
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
								+ " from inventory_master im inner join"
								+ "	(	"
								+ "		select barcode, vendor_id, version_id from inventory_master im_inner "
								+ "		where version_id > (select coalesce (max(version_id), 0) from vendor_item_master where vendor_id = im_inner.vendor_id and barcode = im_inner.barcode) "
								+ " ) in_inner " + "	on im.barcode = in_inner.barcode "
								+ "	and im.version_id = in_inner.version_id"
								+ "	and im.vendor_id = in_inner.vendor_id order by im.id ").addEntity(
						"inventory_master", InventoryMaster.class);

		/*
		 * Query query = session .createSQLQuery( "select im.* " +
		 * " from inventory_master im inner join " +
		 * " 	(select barcode, vendor_id, max(version_id) version_id from inventory_master group by barcode, vendor_id) in_inner "
		 * + " on im.barcode = in_inner.barcode " +
		 * " and im.version_id = in_inner.version_id " +
		 * " and im.vendor_id = in_inner.vendor_id " + " and im.version_id > " +
		 * "		(select coalesce (max(version_id), 0) " +
		 * "		from vendor_item_master " + "		where barcode = im.barcode " +
		 * "		and vendor_id = im.vendor_id) " +
		 * " order by im.id ").addEntity("inventory_master",
		 * InventoryMaster.class);
		 */

		List<InventoryMaster> inventoryMasterList = query.list();

		int updatedInventorySize = 0;
		long processedId = 0L;
		int newInventorySize = inventoryMasterList.size();

		if (newInventorySize < 1) {
			session.clear();
			session.close();
			return;
		}

		LOGGER.info("Looked at the inventory-master table and found [{}] new entries since the last update of data.",
				inventoryMasterList.size());
		LOGGER.debug(
				"Looked at the inventory-master table using query [{}] and found [{}] new entries since the last update of data.",
				query.getQueryString(), inventoryMasterList.size());

		// Did we find anything new?
		for (InventoryMaster instance : inventoryMasterList) {

			/**
			 * Following 5 lines of code is put in to simply careful about
			 * ordering in list traversal.
			 */
			if (instance.getId() < processedId) {
				LOGGER.error("ASSUMPTION WRONG, order by clause is not being maintained while traversing list. Panic!!!");
			} else {
				processedId = instance.getId();
				LOGGER.info("Evaluating im.id [{}]", processedId);
			}

			InventoryMaster imData = (InventoryMaster) instance;

			VendorItemMaster vendorItemMasterNew = new VendorItemMaster();

			Long productId = 0L;
			Long vendorId = imData.getVendorId();
			String barcode = imData.getBarcode();

			LOGGER.trace("Working with data Vendor:Barcode:Version [{}]:[{}]:[{}]", vendorId, barcode,
					imData.getVersionId());

			// First see if the product(s) for this bar-code exists already
			Query pmQuery = session.createSQLQuery("select pm.* from product_master pm where barcode = :barcode ")
					.addEntity("product_master", ProductMaster.class).setParameter("barcode", barcode);

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
			VendorItemHistory vendorItemHistory = null;
			VendorItemMaster vendorItemMasterExisting = null;

			List<VendorItemMaster> vendorItemMasterList = vimQuery.list();

			if (CollectionUtils.isNotEmpty(vendorItemMasterList)) {
				vendorRecordAlreadyExist = true;
				vendorItemMasterExisting = vendorItemMasterList.get(0);
				LOGGER.trace("Found existing Vendor-Barcode-Version record [{}-{}-{}]",
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

				vendorItemHistory = new VendorItemHistory(vendorItemMasterExisting);

				vendorItemMasterExisting.setDiscountJSON(imData.getDiscountJSON());
				vendorItemMasterExisting.setTaxJSON(imData.getTaxJSON());
				vendorItemMasterExisting.setImageJSON(imData.getImageJSON());
				vendorItemMasterExisting.setItemCode(imData.getItemCode());
				vendorItemMasterExisting.setMrp(imData.getMrp());
				vendorItemMasterExisting.setPrice(imData.getPrice());
				vendorItemMasterExisting.setVersionId(imData.getVersionId());
				vendorItemMasterExisting.setName(imData.getName());
				vendorItemMasterExisting.setTagLine(imData.getTagLine());
				vendorItemMasterExisting.setDescription(imData.getDescription());
				vendorItemMasterExisting.setCreatedOn(new java.sql.Date(System.currentTimeMillis()));

			} else {

				vendorItemMasterNew.setVendorId(imData.getVendorId());
				vendorItemMasterNew.setItemCode(imData.getItemCode());
				vendorItemMasterNew.setVersionId(imData.getVersionId());
				vendorItemMasterNew.setBarcode(imData.getBarcode());
				vendorItemMasterNew.setMrp(imData.getMrp());
				vendorItemMasterNew.setPrice(imData.getPrice());
				vendorItemMasterNew.setImageJSON(imData.getImageJSON());
				vendorItemMasterNew.setDiscountJSON(imData.getDiscountJSON());
				vendorItemMasterNew.setTaxJSON(imData.getTaxJSON());
				vendorItemMasterNew.setName(imData.getName());
				vendorItemMasterNew.setTagLine(imData.getTagLine());
				vendorItemMasterNew.setDescription(imData.getDescription());
				vendorItemMasterNew.setCreatedOn(new java.sql.Date(System.currentTimeMillis()));

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
				LOGGER.trace("Is there a change in product information? Answer [{}]",
						existingNewProductAreSame == 0 ? "True" : "False");
				LOGGER.trace(
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

							LOGGER.trace(
									"Pre-existing in 1:1 combination, simply update the product [{}] received for vendor [{}]",
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
						LOGGER.trace("Nothing new to be done for this product/vendor [{}]/[{}] combination", productId,
								vendorId);
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

						LOGGER.trace(
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

				vendorItemHistory.setProductId(productId);

				session.save(vendorItemHistory);
				LOGGER.trace(
						"Updated the master entry and moving the existing record to the history table for product/vendor [{}]/[{}] combination",
						productId, vendorId);

			} else {

				vendorItemMasterNew.setProductId(productId);
				session.save(vendorItemMasterNew);

				/**
				 * TODO Commenting the line below because if the vendor record
				 * never previously existed, there is no need for adding
				 * anything to the history. But since the code change has been
				 * slightly old, don't want to miss out on something now.
				 */
				// vendorItemHistory.setProductId(productId);
				// session.save(vendorItemHistory);
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
	 * accordingly updates the latest inventory caches.
	 */
	@SuppressWarnings("unchecked")
	public void processVendorDetailData(LoadingCache<Long, Long> vendorVersionCache,
			LoadingCache<Long, String> currentInventoryCache) {

		Session session = this.sessionFactory.openSession();

		try {

			/*
			 * Get the vendor-version combinations where latest known version
			 * hasn't been synced
			 */
			Query vendorVersionDetailQuery = session.createSQLQuery(
			// "select vvd.* from vendor_version_detail vvd where vvd.latest_synced_version_id <= (select max(version_id) from vendor_item_master vim where vendor_id = vvd.vendor_id)")
					" select vvd.* from vendor_version_detail vvd ").addEntity("vendor_version_detail",
					VendorVersionDetail.class);

			List<VendorVersionDetail> vvdDataRows = vendorVersionDetailQuery.list();
			LOGGER.debug(
					" Looking at all [{}] vendors, if more recent version of data is available in item-master for inventory cache.",
					vvdDataRows.size());

			// For every vendor where sync in required
			ObjectMapper mapper = new ObjectMapper();

			for (VendorVersionDetail vvdDataRow : vvdDataRows) {

				Long versionId = 0L;
				Long vendorId = vvdDataRow.getVendorId();
				InventoryInfo cachedInventory = new InventoryInfo();
				Long lastSyncVersion = vvdDataRow.getLatestSyncedVersionId();

				Query vendorItemMasterQuery = session
						.createSQLQuery(
								" select vim.* from vendor_item_master vim where vendor_id = :vendorId and version_id >= :lastSyncVersionId order by version_id desc ")
						// " select vim.* from vendor_item_master vim where vendor_id = :vendorId order by version_id desc ")
						.addEntity("vendor_item_master", VendorItemMaster.class).setParameter("vendorId", vendorId)
						.setParameter("lastSyncVersionId", lastSyncVersion);

				List<VendorItemMaster> vendorItemMasterList = vendorItemMasterQuery.list();

				if (vendorItemMasterList == null || vendorItemMasterList.size() < 1) {
					continue;
				}

				LOGGER.trace(
						"Found [{}] available items for vendor [{}] in item-master that may need update in caching/serving",
						vendorItemMasterList.size(), vendorId);

				/**
				 * Assuming order-ids are preserved from result set into the
				 * list, or else we'll need to traverse the whole list to find
				 * the maximum value.
				 * 
				 * Also, this doesn't handle the inventory delete cases yet.
				 */
				versionId = vendorItemMasterList.get(0).getVersionId();
				String currentCachedInventoryStr = vvdDataRow.getCurrentInventory();

				/**
				 * If previously no data existed in the current inventory
				 * column, simply add up everything present in the item-master.
				 * This will mostly be when the vendor is sending the data for
				 * the first time or in case something got erased and getting
				 * completely reset. If the data already exist, update only if
				 * the latest entry has version more than the last-sync version
				 * or else you know it's already contained and updated.
				 */
				if (StringUtils.isNotEmpty(currentCachedInventoryStr)) {

					cachedInventory = mapper.readValue(currentCachedInventoryStr, InventoryInfo.class);
					/*
					 * int itemsAlreadyInCurrentInventory =
					 * cachedInventory.getAddedItems().size();
					 * 
					 * if (itemsAlreadyInCurrentInventory ==
					 * vendorItemMasterList.size()) { LOGGER.trace(
					 * "Existing current inventory entry for vendor-version [{}-{}] up-to-date. Skipping"
					 * , vendorId, versionId); continue; } else {
					 */

					// Set<String> existingItemsInCachedInventory =
					// cachedInventory.getAddedItems().keySet();

					for (VendorItemMaster instance : vendorItemMasterList) {

						String itemCode = instance.getItemCode();
						LOGGER.trace("Evaluating current inventory item [{}] for vendor-version [{}-{}]...", itemCode,
								vendorId, versionId);

						// if
						// (!existingItemsInCachedInventory.contains(itemCode)
						// ||
						// instance.getVersionId().compareTo(lastSyncVersion)>0)
						// {
						if (instance.getVersionId().compareTo(lastSyncVersion) >= 0) {
							LOGGER.debug(
									"Adding item-code [{}] to the existing cache of version [{}] for vendor [{}] to new version [{}]",
									itemCode, lastSyncVersion, vendorId, versionId);
							cachedInventory.getAddedItems().put(instance.getItemCode(), new ItemInfo(instance));
						} else {
							LOGGER.trace("Skipping because of version being lower than last-sync");
						}
					}

					// }
				} else {
					for (VendorItemMaster instance : vendorItemMasterList) {
						cachedInventory.getAddedItems().put(instance.getItemCode(), new ItemInfo(instance));
					}

				}

				cachedInventory.setVendorId(vendorId);
				cachedInventory.setCurrentDataVersionId(versionId);
				cachedInventory.setNewDataVersionId(versionId);

				try {
					vvdDataRow.setCurrentInventory(mapper.writeValueAsString(cachedInventory));
					LOGGER.trace("Working on vendor [{}] whose last and current version ids are [{}] and [{}]",
							vendorId, vvdDataRow.getLatestSyncedVersionId(), versionId);

					// Update the Vendor Version details for updates at some
					// point
					vvdDataRow.setLatestSyncedVersionId(versionId);
					vvdDataRow.setLastModifiedOn(new java.sql.Date(System.currentTimeMillis()));
					vvdDataRow.setValidVersionIds((StringUtils.isEmpty(vvdDataRow.getValidVersionIds()) ? versionId
							.toString() : ((vvdDataRow.getValidVersionIds().contains(versionId.toString()) ? ""
							: vvdDataRow.getValidVersionIds() + "," + versionId))));

					// Update the vendor-version detail first
					session.getTransaction().begin();
					session.saveOrUpdate(vvdDataRow);
					session.merge(vvdDataRow);
					session.getTransaction().commit();

					LOGGER.info(
							"Asking for vendor-version-inventory cache refresh for vendor [{}] from version [{}] to [{}]",
							vendorId, lastSyncVersion, versionId);

					vendorVersionCache.refresh(vendorId);
					currentInventoryCache.refresh(vendorId);

				} catch (Exception e) {
					LOGGER.warn(
							"Exception [{}] occured with error message [{}] occurred while serializing the latest inventory version [{}] for vendor [{}]. \n Skipping this entry for now.",
							e.getClass().getName(), e.getMessage(), versionId, vendorId);
					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			LOGGER.warn(
					"Exception [{}] with message [{}] occurred while generating data for differential data. Eating the exception for it will be retried soon.",
					e.getClass().getName(), e.getMessage());
			e.printStackTrace();
		} finally {
			session.close();
		}

	}

	/*
	 * This function looks at any new vendor specific inventory added and
	 * accordingly updates the differential caches.
	 */
	@SuppressWarnings("unchecked")
	public void processDifferentialData(LoadingCache<String, InventoryInfo> differentialInventoryCache)
			throws JsonProcessingException {

		Session session = this.sessionFactory.openSession();

		try {

			/*
			 * Get the vendor-version combinations where latest known version
			 * hasn't been synced. Later on get data where it matches the max
			 * version to handle cases where part of the update for same version
			 * came in little later.
			 */
			Query vendorVersionDifferentialQuery = session
					.createSQLQuery(
							"select vvdf.* from vendor_version_differential vvdf "
									+ "	where vvdf.is_valid = 't' "
									+ "	and vvdf.last_synced_version_id <= (select max(version_id) from vendor_item_master vim where vendor_id = vvdf.vendor_id) order by vendor_id, version_id ")
					.addEntity("vvdf", VendorVersionDifferential.class);

			List<VendorVersionDifferential> vendorVersionDifferentialList = vendorVersionDifferentialQuery.list();

			LOGGER.debug(
					"[{}] vendors selected where more recent version of data is available in item-master for differential cache.",
					vendorVersionDifferentialList.size());

			// For every know vendor-version detail data
			for (VendorVersionDifferential vendorVersionDifferential : vendorVersionDifferentialList) {

				Long vendorId = vendorVersionDifferential.getVendorId();

				Query vendorMaxVersionQuery = session
						.createSQLQuery(
								" select max(version_id) latestVersionId from vendor_item_master vim where vendor_id = :vendorId ")
						.setParameter("vendorId", vendorId);

				Long currentVersionId = vendorVersionDifferential.getVersionId();
				Long lastSyncVersionId = vendorVersionDifferential.getLastSyncedVersionId();
				Long latestVersionId = Long.parseLong(vendorMaxVersionQuery.list().get(0).toString());

				LOGGER.debug(
						"Working with vendor-version-lastsync [{}]-[{}]-[{}] for updating differential data from last sync version [{}] w.r.t. version [{}].",
						vendorId, vendorVersionDifferential.getVersionId(), currentVersionId, lastSyncVersionId, latestVersionId);

				/**
				 * For the scenarios where the vendor side data is seen for the
				 * first time for a given vendor, the differential is nothing
				 * because only known version in the latest version and there is
				 * no differential data to report. Create an empty row but with
				 * latest version information.
				 */
				if (vendorVersionDifferential.getIsThisLatestVersion() && lastSyncVersionId.longValue() == 0L) {

					LOGGER.info("For vendor [{}], creating first genuine differential record with version [{}]",
							vendorId, latestVersionId);

					vendorVersionDifferential.setVersionId(latestVersionId);
					vendorVersionDifferential.setLastSyncedVersionId(latestVersionId);
					vendorVersionDifferential.setLastModifiedOn(new Date(System.currentTimeMillis()));

					session.getTransaction().begin();
					session.saveOrUpdate(vendorVersionDifferential);
					session.merge(vendorVersionDifferential);
					session.getTransaction().commit();
				}
				/**
				 * Now this is the most recent latest version entry which now
				 * needs to change and contain differentials w.r.t. the latest
				 * data 1. Create a new latest version entry and 2. Update the
				 * now old latest version entry
				 */
				//else if (vendorVersionDifferential.getIsThisLatestVersion()) {
				else if (Boolean.TRUE) {

					/**
					 * Check for the case if the latest data still belongs to
					 * the previously known version in case there was
					 * error/delay in pushing previous version correctly. For
					 * such cases, TODO make sure user receives the notification
					 * in case he downloaded the dirty version and thinks he has
					 * the latest one.
					 */
					if (lastSyncVersionId.longValue() == 0L) {
						/**
						 * For this particular row, do nothing and move on as it's sort of impossible case. May be remove.
						 */
						continue;
					}

					Boolean wasThisTheLatestVersion = vendorVersionDifferential.getIsThisLatestVersion();
					VendorVersionDifferential vendorVersionDifferentialNew = null;
					if(wasThisTheLatestVersion) {
						
						LOGGER.trace("Working on now-old latest version of data and will need to create a new latest version.");
						vendorVersionDifferentialNew = new VendorVersionDifferential();
						vendorVersionDifferentialNew.setVendorId(vendorId);
						vendorVersionDifferentialNew.setVersionId(latestVersionId);
						vendorVersionDifferentialNew.setIsThisLatestVersion(Boolean.TRUE);
						vendorVersionDifferentialNew.setLastSyncedVersionId(latestVersionId);
						vendorVersionDifferentialNew.setLastModifiedOn(new Date(System.currentTimeMillis()));
						
					}

					/**
					 * Now look for the last sync version data details to be
					 * compared with the latest version data details. In case
					 * you can't find that, delete the differential row
					 * completely for rather using the most recent data version
					 * although this is highly unlikely a case. Otherwise,
					 * simply compare and update the previous latest version
					 * entry.
					 * 
					 * First pull all the data corresponding to the last-sync
					 * version which may be in history mostly, specially with
					 * newer versions pushing more data to history. So pull form
					 * history first, and then look at the latest till the
					 * last-sync data version only.
					 */

					ObjectMapper mapper = new ObjectMapper();
					InventoryInfo lastSyncVersionInventory = new InventoryInfo();

					Query vendorDataPushedToHistory = session
							.createSQLQuery(
									" select vih.* "
											+ " from vendor_item_history vih "
											+ "	inner join "
											+ "		(	select barcode, vendor_id, version_id from vendor_item_history vih_inner "
											+ "			where version_id >= (select max(version_id) from vendor_item_history where vendor_id = vih_inner.vendor_id and barcode = vih_inner.barcode and version_id <= :currentVersionId ) "
											+ "		) in_inner " + " on vih.barcode = in_inner.barcode "
											+ " and vih.version_id = in_inner.version_id "
											+ " and vih.vendor_id = in_inner.vendor_id "
											+ " and vih.vendor_id = :vendorId "
											+ " and vih.version_id <= :currentVersionId ")
							.addEntity("vih", VendorItemHistory.class)
							.setParameter("currentVersionId", currentVersionId).setParameter("vendorId", vendorId);

					List<VendorItemHistory> oldItemDataFromHistoryList = vendorDataPushedToHistory.list();
					
					LOGGER.debug("Found [{}] rows of inventory data for vendor-version-lastsync [{}-{}-{}] in history", oldItemDataFromHistoryList.size(), vendorId, currentVersionId, lastSyncVersionId);

					for (VendorItemHistory instance : oldItemDataFromHistoryList) {
						lastSyncVersionInventory.getAddedItems().put(instance.getItemCode(), new ItemInfo(instance));
						LOGGER.trace("Item-code [{}] found from history for version [{}] to be used for comparison", instance.getItemCode(), currentVersionId);
					}

					Query inventoryForVersionQuery = session
							.createSQLQuery(
									" select vim.* from vendor_item_master vim where vendor_id = :vendorId and version_id <= :currentVersionId")
							.addEntity("vim", VendorItemMaster.class).setParameter("vendorId", vendorId)
							.setParameter("currentVersionId", currentVersionId);

					List<VendorItemMaster> lastSyncVersionItemDataList = inventoryForVersionQuery.list();
					
					LOGGER.debug("Found [{}] rows of inventory data for vendor-version-lastsync [{}-{}-{}] in master", lastSyncVersionItemDataList.size(), vendorId, currentVersionId, lastSyncVersionId);

					for (VendorItemMaster instance : lastSyncVersionItemDataList) {
						lastSyncVersionInventory.getAddedItems().put(instance.getItemCode(), new ItemInfo(instance));
						LOGGER.trace("Item-code [{}] found from master for version [{}] to be used for comparison", instance.getItemCode(), currentVersionId);
					}

					if (lastSyncVersionInventory.getAddedItems().size() == 0) {
						LOGGER.warn(
								"While updating vendor [{}] differential data to version [{}], data for the version [{}] is no (longer) available. \nSimply remove this row so that users can pull the latest inventory always.",
								vendorId, latestVersionId, currentVersionId);
						vendorVersionDifferential.setIsValid(Boolean.FALSE);
					} else {

						int totalLastSyncDataRows = lastSyncVersionInventory.getAddedItems().size();

						Query latestVendorDataQuery = session
								.createSQLQuery(
										" select vim.* from vendor_item_master vim where vendor_id = :vendorId and version_id > :currentVersionId")
								.addEntity("vim", VendorItemMaster.class)
								.setParameter("vendorId", vendorId)
								.setParameter("currentVersionId", currentVersionId);

						List<VendorItemMaster> latestItemDataList = latestVendorDataQuery.list();
						LOGGER.debug(
								"Found [{}] rows of latest version [{}] data to compare with [{}] rows of version [{}] for vendor [{}]",
								latestItemDataList.size(), latestVersionId, totalLastSyncDataRows, currentVersionId,
								vendorId);
						
						if(latestItemDataList.size() < 1) {
							/**
							 * There is no new data in the latest versions, hence skipping;
							 */
							continue;
						}

						vendorVersionDifferential.setIsThisLatestVersion(Boolean.FALSE);
						vendorVersionDifferential.setLastSyncedVersionId(latestVersionId);
						vendorVersionDifferential.setLastModifiedOn(new Date(System.currentTimeMillis()));

						Map<String, ItemInfo> lastSyncItemDataMap = new HashMap<String, ItemInfo>();

						for (String key : lastSyncVersionInventory.getAddedItems().keySet()) {
							ItemInfo itemInfo = lastSyncVersionInventory.getAddedItems().get(key);
							lastSyncItemDataMap.put(itemInfo.getItemCode(), itemInfo);
							LOGGER.trace("Adding item-code [{}] in the original inventory for version [{}] to be used for comparison", itemInfo.getItemCode(), currentVersionId);
						}

						StringBuffer deltaItemCodes = new StringBuffer();

						InventoryInfo newInventoryInfo = new InventoryInfo();

						newInventoryInfo.setVendorId(vendorId);
						newInventoryInfo.setCurrentDataVersionId(vendorVersionDifferential.getVersionId());
						newInventoryInfo.setNewDataVersionId(latestVersionId);

						for (VendorItemMaster latestItemData : latestItemDataList) {

							String itemCode = latestItemData.getItemCode();
							ItemInfo latestItemInfo = new ItemInfo(latestItemData);
							ItemInfo lastSyncItemInfo = lastSyncItemDataMap.get(itemCode);

							deltaItemCodes.append(itemCode + Constants.COMMA_SEPARATOR);

							/**
							 * Comparing an item code among two version, if
							 * missing in older version, add to the Added list,
							 * or else update the entry. Note that if not found
							 * in latest version instead, add to the deleted
							 * entry.
							 */
							if (lastSyncItemInfo == null) {
								LOGGER.trace(
										"Item [{}] was not found in the version [{}] for vendor [{}], so adding it from latest version [{}]",
										itemCode, currentVersionId, vendorId, latestVersionId);
								newInventoryInfo.getAddedItems().put(itemCode, latestItemInfo);
								continue;
							}

							LOGGER.trace(
									"Item [{}] found in the last sync version [{}] for vendor [{}], so updatng it from latest version [{}]",
									itemCode, currentVersionId, vendorId, latestVersionId);
							newInventoryInfo.getUpdatedItems().put(itemCode,
									lastSyncItemInfo.generateDifferentialFrom(latestItemInfo));

						}

						/**
						 * For now let's not handle items remaining in the
						 * lastSync bucket because there is no clear notion of
						 * delete in the inventory-master.
						 */

						vendorVersionDifferential.setDifferentialData(mapper.writeValueAsString(newInventoryInfo));

						String deltaItemCodesStr = deltaItemCodes.toString();
						vendorVersionDifferential.setDeltaItemCodes(deltaItemCodesStr.substring(0,
								deltaItemCodesStr.lastIndexOf(Constants.COMMA_SEPARATOR)));

						LOGGER.info("Updated differential data row for vendor-version [{}-{}] from version [{}] to [{}]",
								vendorId, currentVersionId, lastSyncVersionId, latestVersionId);

					}

					session.getTransaction().begin();
					
					if(wasThisTheLatestVersion) {
						LOGGER.info("Adding new differential data row for vendor [{}] version [{}]", vendorId,
								latestVersionId);
						session.save(vendorVersionDifferentialNew);
					}
					session.saveOrUpdate(vendorVersionDifferential);
					session.merge(vendorVersionDifferential);
					session.getTransaction().commit();
					
					differentialInventoryCache.refresh(vendorId+Constants.CACHE_KEY_SEPARATOR_STRING+currentVersionId);

				}
				/**
				 * Work with one of the older version entry
				 */
				else {

					ObjectMapper mapper = new ObjectMapper();
					InventoryInfo lastSyncVersionInventory = new InventoryInfo();

					Query inventoryForVersionQuery = session
							.createSQLQuery(
									" select vim.* from vendor_item_master vim where vendor_id = :vendorId and version_id <= :lastSyncedVersionId ")
							.addEntity("vim", VendorItemMaster.class).setParameter("vendorId", vendorId)
							.setParameter("lastSyncedVersionId", vendorVersionDifferential.getLastSyncedVersionId());

					List<VendorItemMaster> lastSyncVersionItemDataList = inventoryForVersionQuery.list();

					for (VendorItemMaster instance : lastSyncVersionItemDataList) {
						lastSyncVersionInventory.getAddedItems().put(instance.getItemCode(), new ItemInfo(instance));
					}

					if (lastSyncVersionInventory == null || lastSyncVersionInventory.getAddedItems().size() == 0) {
						LOGGER.warn(
								"While updating vendor [{}] differential data to version [{}], data for the last sync version [{}] is no (longer) available. \nSimply remove this row so that users can pull the latest inventory always.",
								vendorId, latestVersionId, lastSyncVersionId);
						vendorVersionDifferential.setIsValid(Boolean.FALSE);
					} else {

						int totalLastSyncDataRows = lastSyncVersionInventory.getAddedItems().size();

						LOGGER.debug(
								"Found [{}] rows of last version [{}] data to compare with latest data version [{}].",
								totalLastSyncDataRows, lastSyncVersionId, latestVersionId);

						Query latestVendorDataQuery = session
								.createSQLQuery(
								// " select vim.* from vendor_item_master vim where vendor_id = :vendorId and version_id = :latestVersionId ")
										" select vim.* from vendor_item_master vim where vendor_id = :vendorId and version_id >= :lastSyncVersionId ")
								.addEntity("vim", VendorItemMaster.class).setParameter("vendorId", vendorId)
								.setParameter("lastSyncVersionId", lastSyncVersionId);

						List<VendorItemMaster> latestItemDataList = latestVendorDataQuery.list();

						LOGGER.debug(
								"Found [{}] rows of latest version [{}] data to compare with [{}] rows of previous latest version [{}]",
								latestItemDataList.size(), latestVersionId, totalLastSyncDataRows, lastSyncVersionId);

						InventoryInfo previousDifferentialInventoryData = null;
						previousDifferentialInventoryData = mapper.readValue(
								vendorVersionDifferential.getDifferentialData(), InventoryInfo.class);

						LOGGER.debug(
								"Previous differential data for version [{}] has [{}] Added, [{}] Updated and [{}] Deleted items from last sync version [{}]",
								previousDifferentialInventoryData.getCurrentDataVersionId(),
								previousDifferentialInventoryData.getAddedItems().size(),
								previousDifferentialInventoryData.getUpdatedItems().size(),
								previousDifferentialInventoryData.getDeletedItems().size(),
								previousDifferentialInventoryData.getNewDataVersionId());

						String previousDeltaItemCodes = vendorVersionDifferential.getDeltaItemCodes();
						StringBuffer deltaItemCodes = new StringBuffer(previousDeltaItemCodes);

						Map<String, ItemInfo> lastSyncItemDataMap = new HashMap<String, ItemInfo>();

						for (String key : lastSyncVersionInventory.getAddedItems().keySet()) {
							ItemInfo itemInfo = lastSyncVersionInventory.getAddedItems().get(key);
							lastSyncItemDataMap.put(itemInfo.getItemCode(), itemInfo);
						}

						LOGGER.debug(
								"Post conversion to map, found [{}] items in the last synced vendor version [{}] which will now be compared to latest data",
								lastSyncItemDataMap.size(), lastSyncVersionId);

						for (VendorItemMaster latestItemData : latestItemDataList) {

							String itemCode = latestItemData.getItemCode();
							LOGGER.trace("Working with item-code [{}] from the latest vendor-version [{}]-[{}]",
									itemCode, vendorId, latestVersionId);

							ItemInfo lastSyncItemData = lastSyncItemDataMap.get(itemCode);

							if (lastSyncItemData == null) {
								/**
								 * New item got added to the latest version.
								 * There is no chance this could be
								 * added/updated to the differential but might
								 * be present in deleted folder, so simply
								 * remove if present there and added to updated
								 * list. Since original version data is not
								 * being looked at, simply add the complete data
								 * to the updated list.
								 */

								LOGGER.debug(
										"No data found for item-code [{}] in the last synced vendor version [{}]. Unless previously deleted, will simply be added. ",
										itemCode, lastSyncVersionId);

								if (previousDifferentialInventoryData.getDeletedItems().contains(itemCode)) {
									previousDifferentialInventoryData.getDeletedItems().remove(itemCode);
									// TODO Or pull form history and compare
									// properly
									previousDifferentialInventoryData.getUpdatedItems().put(itemCode,
											new ItemInfo(latestItemData));
								}
								/**
								 * If not found in the deleted list, consider
								 * new addition and add to the "Added" list.
								 * There is no chance of this missing item from
								 * last version to be in added/updated list.
								 */
								else {
									previousDifferentialInventoryData.getAddedItems().put(itemCode,
											new ItemInfo(latestItemData));
									deltaItemCodes.append(itemCode + Constants.COMMA_SEPARATOR);
								}

								continue;
							}

							ItemInfo latestItemInfo = new ItemInfo(latestItemData);

							if (previousDifferentialInventoryData.getAddedItems().keySet().contains(itemCode)) {

								/**
								 * Previously the item was added, and now
								 * updated in the latest version. Now w.r.t. the
								 * original version, it's still an added item,
								 * so simply take the latest data and put that
								 * in the added bucket.
								 */

								LOGGER.debug(
										"This item was previously added in differential, so simply adding the latest version [{}] of this item [{}]",
										latestVersionId, itemCode);
								previousDifferentialInventoryData.getAddedItems().replace(itemCode, latestItemInfo);

							} else if (previousDifferentialInventoryData.getDeletedItems().contains(itemCode)) {
								// Not possible
							} else {

								/**
								 * To keep the things simple, just get the data
								 * from DB and calculate the delta.
								 */

								LOGGER.debug("This item was previously updated or missing in differential, so need to do heavy processing....");

								Query oldVendorDataQuery = session
										.createSQLQuery(
												" select vih.* from vendor_item_history vih where vendor_id = :vendorId and item_code = :itemCode and version_id = (select max(version_id) from vendor_item_history vih where vendor_id = :vendorId and version_id <= :versionId and item_code = :itemCode)   ")
										.addEntity("vih", VendorItemHistory.class)
										.setParameter("versionId",
												previousDifferentialInventoryData.getCurrentDataVersionId())
										.setParameter("vendorId", vendorId).setParameter("itemCode", itemCode);

								List<VendorItemHistory> oldItemDataList = oldVendorDataQuery.list();
								LOGGER.debug("Pulled old data from history with version [{}] and found [{}] rows",
										previousDifferentialInventoryData.getCurrentDataVersionId(),
										(oldItemDataList == null ? "NULL" : oldItemDataList.size()));

								ItemInfo oldItemInfo = null;

								if (CollectionUtils.isNotEmpty(oldItemDataList)) {
									oldItemInfo = new ItemInfo(oldItemDataList.get(0));
								}

								if (oldItemInfo == null) {
									/**
									 * Although very unlikely case but just in
									 * case the original data is not available
									 * form the DB, make a new Added entry for
									 * the version into the differential data.
									 */

									LOGGER.debug(
											"Looked up original data from history for item-code [{}] using version [{}], but found nothing. Adding it as fresh item from latest version [{}]",
											itemCode, previousDifferentialInventoryData.getCurrentDataVersionId(),
											latestVersionId);
									previousDifferentialInventoryData.getAddedItems().put(itemCode, latestItemInfo);

								} else {

									LOGGER.debug(
											"Looked up original data from history for item-code [{}] using version [{}], and found the data",
											itemCode, previousDifferentialInventoryData.getCurrentDataVersionId());

									previousDifferentialInventoryData.getUpdatedItems().put(itemCode,
											oldItemInfo.generateDifferentialFrom(latestItemInfo));
								}

								/**
								 * If this wasn't previously contained in the
								 * delta of item codes, add it
								 */
								if (!previousDeltaItemCodes.contains(itemCode)) {
									deltaItemCodes.append(itemCode + Constants.COMMA_SEPARATOR);
								}

							}

						}

						/**
						 * For now let's not handle items remaining in the
						 * lastSync bucket because there is no clear notion of
						 * delete in the inventory-master.
						 */

						previousDifferentialInventoryData.setNewDataVersionId(latestVersionId);

						String deltaItemCodesStr = deltaItemCodes.toString();
						if (StringUtils.isNotEmpty(deltaItemCodesStr)
								&& deltaItemCodesStr.lastIndexOf(Constants.COMMA_SEPARATOR) > 0) {
							vendorVersionDifferential.setDeltaItemCodes(deltaItemCodesStr.substring(0,
									deltaItemCodesStr.lastIndexOf(Constants.COMMA_SEPARATOR)));
						}

						vendorVersionDifferential.setDifferentialData(mapper
								.writeValueAsString(previousDifferentialInventoryData));

						vendorVersionDifferential.setLastSyncedVersionId(latestVersionId);
						vendorVersionDifferential.setLastModifiedOn(new Date(System.currentTimeMillis()));

						session.getTransaction().begin();
						session.saveOrUpdate(vendorVersionDifferential);
						session.merge(vendorVersionDifferential);
						session.getTransaction().commit();

						LOGGER.info(
								"Updated differential data row for vendor [{}] from version [{}] to [{}]. \nAsking for cache refresh.",
								vendorId, lastSyncVersionId, latestVersionId);

					}
				}

				LOGGER.debug("Finished handling the differential data for venrdor-version-lastversion [{}]-[{}]-[{}]",
						vendorVersionDifferential.getVendorId(), vendorVersionDifferential.getVersionId(),
						vendorVersionDifferential.getLastSyncedVersionId());

			}

		} catch (Exception e) {
			LOGGER.warn(
					"Exception [{}] with message [{}] occurred while generating data for differential data. Eating the exception for it will be retried soon.",
					e.getClass().getName(), e.getMessage());
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

}
