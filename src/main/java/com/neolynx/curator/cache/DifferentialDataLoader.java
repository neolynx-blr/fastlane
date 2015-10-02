package com.neolynx.curator.cache;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheLoader;
import com.neolynx.common.model.InventoryResponse;
import com.neolynx.common.model.ItemResponse;
import com.neolynx.curator.core.VendorItemMaster;
import com.neolynx.curator.core.VendorVersionDifferential;
import com.neolynx.curator.util.Constants;
import com.neolynx.curator.util.StringUtilsCustom;

/**
 * Created by nitesh.garg on 06-Sep-2015
 */
public class DifferentialDataLoader extends CacheLoader<String, InventoryResponse> {

	private SessionFactory sessionFactory;
	static Logger LOGGER = LoggerFactory.getLogger(DifferentialDataLoader.class);

	public DifferentialDataLoader(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.common.cache.CacheLoader#load(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@UnitOfWork
	@Override
	public InventoryResponse load(String vendorVersionKey) throws Exception {

		if (vendorVersionKey == null) {
			LOGGER.debug("Tried loading differential data for NULL vendor-version-id combination, obviously failed.");
			return null;
		}

		InventoryResponse inventoryResponse = new InventoryResponse();
		List<String> vendorVersion = new StrTokenizer(vendorVersionKey, Constants.VENDOR_VERSION_KEY_SEPARATOR)
				.getTokenList();

		Long vendorId = Long.parseLong(vendorVersion.get(0));
		Long versionId = Long.parseLong(vendorVersion.get(1));

		LOGGER.debug("Looking to load differential cache for vendor-version [{}-{}]", vendorId, versionId);

		inventoryResponse.setVendorId(vendorId);
		inventoryResponse.setCurrentDataVersionId(versionId);

		Session session = sessionFactory.openSession();

		// Check the DB data for this vendor-version combination
		Query diffQuery = session
				.createSQLQuery(
						" select vvd.* from vendor_version_differential vvd where vendor_id = :vendorId and version_id = :versionId ")
				.addEntity("vendor_version_differential", VendorVersionDifferential.class)
				.setParameter("vendorId", vendorId).setParameter("versionId", versionId);

		List<VendorVersionDifferential> vendorVersionDifferentials = diffQuery.list();

		if (CollectionUtils.isEmpty(vendorVersionDifferentials)) {
			LOGGER.debug("Unable to find DB entry for Vendor-Version [{}-{}]", vendorId, versionId);
		} else {

			List<VendorItemMaster> vendorItemMasterList;

			VendorVersionDifferential diffInstance = vendorVersionDifferentials.get(0);
			Long lastSyncedVersionId = diffInstance.getLastSyncedVersionId();

			inventoryResponse.setNewDataVersionId(lastSyncedVersionId);

			/*
			 * Check the version you are trying to load against the latest know
			 * data version for that vendor. If same version, simply load the
			 * latest data for that version, or else looks at the item code
			 * differentials and load the data accordingly. Note that in case
			 * the vendor_item_master has got further updated in the meanwhile,
			 * nothing will be returned from these queries but the cache will
			 * eventually be refreshed.
			 * 
			 * TODO This can potentially lead to older version returned for
			 * slightly longer. This should add check on the client side to
			 * double check latest version at the time of order finalisation
			 * and/or submission.
			 */
			if (versionId.compareTo(lastSyncedVersionId) == 0) {

				Query query = session
						.createSQLQuery(
								"select vim.* from vendor_item_master vim where vendor_id = :vendorId and version_id <= :lastSyncedVersionId "
										+ " order by vim.item_code ")
						.addEntity("vendor_item_master", VendorItemMaster.class).setParameter("vendorId", vendorId)
						.setParameter("lastSyncedVersionId", lastSyncedVersionId);

				vendorItemMasterList = query.list();
				LOGGER.debug("For Vendor [{}], pulling the latest inventory containing [{}] items", vendorId,
						vendorItemMasterList.size());

			} else {

				Query query = session
						.createSQLQuery(
								"select vim.* from vendor_item_master vim where vendor_id = :vendorId and version_id <= :lastSyncedVersionId and item_code in (:itemCodes) "
										+ " order by vim.item_code ")
						.addEntity("vendor_item_master", VendorItemMaster.class)
						.setParameter("vendorId", vendorId)
						.setParameter("lastSyncedVersionId", lastSyncedVersionId)
						.setParameter("itemCodes",
								StringUtilsCustom.convertStringToTokens(diffInstance.getDeltaItemCodes()));

				vendorItemMasterList = query.list();
				LOGGER.debug("For Vendor [{}] found [{}] items as differential", vendorId, vendorItemMasterList.size());

			}

			if (CollectionUtils.isNotEmpty(vendorItemMasterList)) {
				inventoryResponse.setItemsUpdated(new ArrayList<ItemResponse>());
				LOGGER.debug("New differential data found for vendor-version [{}-{}]", vendorId, versionId);
			}

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
				LOGGER.debug("Adding new differential data with item-code [{}] for vendor-version [{}-{}]",
						itemData.getItemCode(), vendorId, versionId);
			}
		}

		session.close();
		inventoryResponse.setIsError(Boolean.FALSE);
		return inventoryResponse;
	}

}
