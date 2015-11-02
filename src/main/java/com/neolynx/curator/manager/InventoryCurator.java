package com.neolynx.curator.manager;

import java.io.IOException;
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
 * Meant for curating any newly uploaded inventory into the master tables from
 * which the differentials are calculated and served
 * 
 * 
 * Created by nitesh.garg on 06-Sep-2015
 */

public class InventoryCurator {

	private final SessionFactory sessionFactory;
	private final LoadingCache<Long, String> currentInventoryCache;

	static Logger LOGGER = LoggerFactory.getLogger(InventoryCurator.class);

	public InventoryCurator(SessionFactory sessionFactory, LoadingCache<Long, String> currentInventoryCache) {
		super();
		this.sessionFactory = sessionFactory;
		this.currentInventoryCache = currentInventoryCache;
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

			VendorItemMaster vendorItemMasterNew = new VendorItemMaster();

			Long productId = 0L;
			Long vendorId = imData.getVendorId();
			String barcode = imData.getBarcode();

			LOGGER.debug("Working with data Vendor:Barcode:Version [{}]:[{}]:[{}]", vendorId, barcode,
					imData.getVersionId());

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
			VendorItemHistory vendorItemHistory = null;
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

				vendorItemHistory.setProductId(productId);

				session.save(vendorItemHistory);

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
	 * accordingly updates the caches.
	 */
	@SuppressWarnings("unchecked")
	public void processVendorDetailData(LoadingCache<Long, Long> vendorVersionCache) {

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

			Long versionId = 0L;
			Long vendorId = vvdDataRow.getVendorId();
			InventoryInfo cachedInventory = new InventoryInfo();

			ObjectMapper mapper = new ObjectMapper();
			Query vendorItemMasterQuery = session
					.createSQLQuery(" select vim.* from vendor_item_master vim where vendor_id = :vendorId ")
					.addEntity("vendor_item_master", VendorItemMaster.class).setParameter("vendorId", vendorId);

			List<VendorItemMaster> vendorItemMasterList = vendorItemMasterQuery.list();

			if (CollectionUtils.isEmpty(vendorItemMasterList)) {
				/**
				 * Very unlikely because we just checked for the entries where
				 * max version is higher in the item master table.
				 */
			}

			for (VendorItemMaster instance : vendorItemMasterList) {
				if (instance.getVersionId() > versionId) {
					versionId = instance.getVersionId();
				}
				cachedInventory.getAddedItems().put(instance.getItemCode(), new ItemInfo(instance));
			}

			cachedInventory.setVendorId(vendorId);
			cachedInventory.setCurrentDataVersionId(versionId);
			cachedInventory.setNewDataVersionId(versionId);

			try {
				vvdDataRow.setCurrentInventory(mapper.writeValueAsString(cachedInventory));
			} catch (Exception e) {
				LOGGER.debug(
						"Error [{}] occurred while serializing the latest inventory version [{}] for vendor [{}] and storing in DB",
						versionId, vendorId);
				e.printStackTrace();
			}

			LOGGER.debug("Working on vendor [{}] whose last and current version ids are [{}] and [{}]", vendorId,
					vvdDataRow.getLatestSyncedVersionId(), versionId);

			// Update the Vendor Version details for updates at some point
			vvdDataRow.setLatestSyncedVersionId(versionId);
			vvdDataRow.setLastModifiedOn(new java.sql.Date(System.currentTimeMillis()));
			vvdDataRow.setValidVersionIds((StringUtils.isEmpty(vvdDataRow.getValidVersionIds()) ? versionId.toString()
					: vvdDataRow.getValidVersionIds() + "," + versionId));

			// Update the vendor-version detail first
			session.getTransaction().begin();
			session.saveOrUpdate(vvdDataRow);
			session.merge(vvdDataRow);
			session.getTransaction().commit();

			LOGGER.debug("Refreshing the cache entry for vendor [{}]", vendorId);
			vendorVersionCache.refresh(vendorId);

		}

		session.close();

	}

	/*
	 * This function looks at any new vendor specific inventory added and
	 * accordingly updates the caches.
	 */
	@SuppressWarnings("unchecked")
	public void processDifferentialData(LoadingCache<String, InventoryInfo> differentialInventoryCache)
			throws JsonProcessingException {

		Session session = this.sessionFactory.openSession();

		try {

			/*
			 * Get the vendor-version combinations where latest known version
			 * hasn't been synced
			 */

			Query vendorVersionDifferentialQuery = session
					.createSQLQuery(
							"select vvdf.* from vendor_version_differential vvdf where vvdf.is_valid = 't' and vvdf.last_synced_version_id < (select max(version_id) from vendor_item_master vim where vendor_id = vvdf.vendor_id) order by vendor_id, version_id ")
					.addEntity("vvdf", VendorVersionDifferential.class);

			List<VendorVersionDifferential> vendorVersionDifferentialList = vendorVersionDifferentialQuery.list();

			LOGGER.debug(
					"[{}] vendors selected from vendor-version-differential tables to be synced up with the latest version.",
					vendorVersionDifferentialList.size());

			// For every know vendor-version detail data
			for (VendorVersionDifferential vendorVersionDifferential : vendorVersionDifferentialList) {

				Long vendorId = vendorVersionDifferential.getVendorId();

				Query vendorMaxVersionQuery = session
						.createSQLQuery(
								" select max(version_id) latestVersionId from vendor_item_master vim where vendor_id = :vendorId ")
						.setParameter("vendorId", vendorId);

				Long lastSyncVersionId = vendorVersionDifferential.getLastSyncedVersionId();
				Long latestVersionId = Long.parseLong(vendorMaxVersionQuery.list().get(0).toString());

				LOGGER.debug(
						"Working with vendor-version [{}]-[{}] for updating differential data from last sync version [{}] w.r.t. version [{}].",
						vendorId, vendorVersionDifferential.getVersionId(), lastSyncVersionId, latestVersionId);

				/**
				 * For the scenarios where the vendor side data is seen for the
				 * first time for a given vendor, the differential is nothing
				 * because only known version in the latest version and there is
				 * no differential data to report. Create an empty row but with
				 * latest version information.
				 */
				if (vendorVersionDifferential.getIsThisLatestVersion() && lastSyncVersionId.longValue() == 0L) {

					LOGGER.debug(
							"It's the latest version entry with last synced version [{}] is 0 indicating new vendor data load, hence creating the first genuine differential record with latest version [{}]",
							lastSyncVersionId, latestVersionId);

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
				else if (vendorVersionDifferential.getIsThisLatestVersion()) {

					LOGGER.debug("Working on now-old latest version of data and will need to create a new latest version.");
					VendorVersionDifferential vendorVersionDifferentialNew = new VendorVersionDifferential();
					vendorVersionDifferentialNew.setVendorId(vendorId);
					vendorVersionDifferentialNew.setVersionId(latestVersionId);
					vendorVersionDifferentialNew.setIsThisLatestVersion(Boolean.TRUE);
					vendorVersionDifferentialNew.setLastSyncedVersionId(latestVersionId);
					vendorVersionDifferentialNew.setLastModifiedOn(new Date(System.currentTimeMillis()));

					/**
					 * Now look for the last sync version data details to be
					 * compared with the latest version data details. In case
					 * you can't find that, delete the differential row
					 * completely for rather using the most recent data version
					 * although this is highly unlikely a case. Otherwise,
					 * simply compare and update the previous latest version
					 * entry.
					 */

					ObjectMapper mapper = new ObjectMapper();
					InventoryInfo lastSyncVersionInventory = null;

					String currentLastSyncVersionCache = this.currentInventoryCache.get(vendorId);
					if (StringUtils.isNotEmpty(currentLastSyncVersionCache)) {
						lastSyncVersionInventory = mapper.readValue(currentLastSyncVersionCache, InventoryInfo.class);
					} else {
						// TODO Pull from DB
					}

					if (lastSyncVersionInventory == null || lastSyncVersionInventory.getAddedItems().size() == 0) {

						LOGGER.debug(
								"Item data against the last sync version [{}] is no (longer) available, simply remove this row so that users can pull the latest inventory always.",
								lastSyncVersionId);
						vendorVersionDifferential.setIsValid(Boolean.FALSE);

					} else {

						int totalLastSyncDataRows = lastSyncVersionInventory.getAddedItems().size();

						LOGGER.debug("Found [{}] rows of last version [{}] data to compare with latest data",
								totalLastSyncDataRows, lastSyncVersionId);

						Query latestVendorDataQuery = session
								.createSQLQuery(
										" select vim.* from vendor_item_master vim where vendor_id = :vendorId and version_id = :latestVersionId")
								.addEntity("vim", VendorItemMaster.class).setParameter("vendorId", vendorId)
								.setParameter("latestVersionId", latestVersionId);

						List<VendorItemMaster> latestItemDataList = latestVendorDataQuery.list();
						LOGGER.debug(
								"Found [{}] rows of latest version [{}] data to compare with [{}] rows of previous latest version [{}]",
								latestItemDataList.size(), latestVersionId, totalLastSyncDataRows, lastSyncVersionId);

						vendorVersionDifferential.setIsThisLatestVersion(Boolean.FALSE);
						vendorVersionDifferential.setLastSyncedVersionId(latestVersionId);
						vendorVersionDifferential.setLastModifiedOn(new Date(System.currentTimeMillis()));

						Map<String, ItemInfo> lastSyncItemDataMap = new HashMap<String, ItemInfo>();

						for (String key : lastSyncVersionInventory.getAddedItems().keySet()) {
							ItemInfo itemInfo = lastSyncVersionInventory.getAddedItems().get(key);
							lastSyncItemDataMap.put(itemInfo.getItemCode(), itemInfo);
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
								newInventoryInfo.getAddedItems().put(itemCode, latestItemInfo);
								continue;
							}

							lastSyncItemDataMap.remove(itemCode);

							newInventoryInfo.getUpdatedItems().put(itemCode,
									lastSyncItemInfo.generateDifferentialFrom(latestItemInfo));

						}

						/**
						 * For now let's not handle items remaining in the
						 * lastSync bucket because there is no clear notion of
						 * delete in the inventory-master.
						 */

						/*
						 * 
						 * for (String pendingItemCode :
						 * lastSyncItemDataMap.keySet()) {
						 * 
						 * deltaItemCodes.append(pendingItemCode +
						 * Constants.COMMA_SEPARATOR);
						 * newInventoryInfo.getDeletedItems
						 * ().add(pendingItemCode);
						 * 
						 * }
						 */
						vendorVersionDifferential.setDifferentialData(mapper.writeValueAsString(newInventoryInfo));

						String deltaItemCodesStr = deltaItemCodes.toString();
						vendorVersionDifferential.setDeltaItemCodes(deltaItemCodesStr.substring(0,
								deltaItemCodesStr.lastIndexOf(Constants.COMMA_SEPARATOR)));

						LOGGER.debug(
								"Completed updating differential data row for vendor [{}] with version [{}] to version [{}]",
								vendorId, lastSyncVersionId, latestVersionId);

					}

					LOGGER.debug("Adding new differential data row for vendor [{}] version [{}]", vendorId,
							latestVersionId);
					session.getTransaction().begin();
					session.save(vendorVersionDifferentialNew);

					session.saveOrUpdate(vendorVersionDifferential);
					session.merge(vendorVersionDifferential);
					session.getTransaction().commit();

				}
				/**
				 * Work with one of the older version entry
				 */
				else {

					ObjectMapper mapper = new ObjectMapper();
					InventoryInfo lastSyncVersionInventory = null;

					String currentLastSyncVersionCache = this.currentInventoryCache.get(vendorId);
					if (StringUtils.isNotEmpty(currentLastSyncVersionCache)) {
						lastSyncVersionInventory = mapper.readValue(currentLastSyncVersionCache, InventoryInfo.class);
					} else {
						// TODO Pull from DB
					}

					if (lastSyncVersionInventory == null || lastSyncVersionInventory.getAddedItems().size() == 0) {
						// TODO Pull from DB

						LOGGER.debug(
								"Item data against the last sync version [{}] is no (longer) available, simply remove this row so that users can pull the latest inventory always.",
								lastSyncVersionId);
						vendorVersionDifferential.setIsValid(Boolean.FALSE);

					} else {

						int totalLastSyncDataRows = lastSyncVersionInventory.getAddedItems().size();

						LOGGER.debug("Found [{}] rows of last version [{}] data to compare with latest data.",
								totalLastSyncDataRows, lastSyncVersionId);

						Query latestVendorDataQuery = session
								.createSQLQuery(
										" select vim.* from vendor_item_master vim where vendor_id = :vendorId and version_id = :latestVersionId")
								.addEntity("vim", VendorItemMaster.class).setParameter("vendorId", vendorId)
								.setParameter("latestVersionId", latestVersionId);

						List<VendorItemMaster> latestItemDataList = latestVendorDataQuery.list();

						LOGGER.debug(
								"Found [{}] rows of latest version [{}] data to compare with [{}] rows of previous latest version [{}]",
								latestItemDataList.size(), latestVersionId, totalLastSyncDataRows, lastSyncVersionId);

						InventoryInfo previousDifferentialInventoryData = null;
						try {
							previousDifferentialInventoryData = mapper.readValue(
									vendorVersionDifferential.getDifferentialData(), InventoryInfo.class);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

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
								"Found [{}] items in the last synced vendor version [{}] which will now be compared to latest data",
								lastSyncItemDataMap.size(), lastSyncVersionId);

						for (VendorItemMaster latestItemData : latestItemDataList) {

							String itemCode = latestItemData.getItemCode();
							LOGGER.debug("Working with item-code [{}] from the latest vendor-version [{}]-[{}]",
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

								LOGGER.debug("No data found for item-code [{}] in the last synced vendor version [{}]",
										itemCode, lastSyncVersionId);

								if (previousDifferentialInventoryData.getDeletedItems().contains(itemCode)) {
									previousDifferentialInventoryData.getDeletedItems().remove(itemCode);
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

							lastSyncItemDataMap.remove(itemCode);
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

								LOGGER.debug("This item was previously updated or missing in differential, so need to do heavy processing.");

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

									try {
										previousDifferentialInventoryData.getUpdatedItems().put(itemCode,
												oldItemInfo.generateDifferentialFrom(latestItemInfo));
									} catch (Exception e) {
										LOGGER.debug(
												"Found error while generating differential of item-info with message [{}]",
												e.getMessage());
										e.printStackTrace();
									}
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

						/*
						 * for (String pendingItemCode :
						 * lastSyncItemDataMap.keySet()) {
						 * 
						 * if
						 * (previousDifferentialInventoryData.getAddedItems().
						 * keySet ().contains(pendingItemCode)) {
						 * previousDifferentialInventoryData
						 * .getAddedItems().remove(pendingItemCode); } else if
						 * (previousDifferentialInventoryData
						 * .getUpdatedItems().
						 * keySet().contains(pendingItemCode)) {
						 * previousDifferentialInventoryData
						 * .getUpdatedItems().remove(pendingItemCode);
						 * previousDifferentialInventoryData
						 * .getDeletedItems().add(pendingItemCode); }
						 * 
						 * deltaItemCodes.append(pendingItemCode +
						 * Constants.COMMA_SEPARATOR);
						 * 
						 * }
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

						LOGGER.debug(
								"Completed updating differential data row for vendor [{}] with version [{}] to version [{}]",
								vendorId, lastSyncVersionId, latestVersionId);

						LOGGER.info("Refreshing the cache entry for vendor [{}]", vendorId);
						differentialInventoryCache.refresh(vendorId + "-" + latestVersionId);
					}
				}

				LOGGER.debug("Finished handling the differential data for venrdor-version-lastversion [{}]-[{}]-[{}]",
						vendorVersionDifferential.getVendorId(), vendorVersionDifferential.getVersionId(),
						vendorVersionDifferential.getLastSyncedVersionId());

			}

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.debug("Something happened somewhere with message [{}]", e.getMessage());
		} finally {
			session.close();
		}
	}

}
