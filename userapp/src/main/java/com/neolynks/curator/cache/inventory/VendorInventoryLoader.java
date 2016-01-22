package com.neolynks.curator.cache.inventory;

import com.neolynks.api.common.inventory.ItemInfo;
import io.dropwizard.hibernate.UnitOfWork;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynks.curator.core.VendorItemMaster;
import com.neolynks.curator.util.Constants;

/**
 * Created by nitesh.garg on Dec 28, 2015
 *
 */
public class VendorInventoryLoader implements CacheLoader<String, ItemInfo> {

	private SessionFactory sessionFactory;
	static Logger LOGGER = LoggerFactory.getLogger(VendorInventoryLoader.class);

	public VendorInventoryLoader(SessionFactory sessionFactory) {
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
	public ItemInfo load(String vendorIdBarcode) throws Exception {

		ItemInfo vendorBarcodeItemInfo = null;
		if (vendorIdBarcode == null) {
			LOGGER.debug("Tried loading all inventory for NULL vendor-id, obviously failed.");
		} else {
			
			Long vendorId = Long.parseLong(vendorIdBarcode.substring(0, vendorIdBarcode.indexOf(Constants.HYPHEN_SEPARATOR)));
			Long barcode = Long.parseLong(vendorIdBarcode.substring(vendorIdBarcode.indexOf(Constants.HYPHEN_SEPARATOR)+1));

			Session session = sessionFactory.openSession();

			Query vendorBarcodeDetailQuery = session
					.createSQLQuery(
							" select vvd.* from vendor_item_master vim where vendor_id = :vendorId and barcode = :barcode ")
					.addEntity("vendor_item_master", VendorItemMaster.class).setParameter("vendorId", vendorId)
					.setParameter("barcode", barcode);

			List<VendorItemMaster> vendorBarcodeDetails = vendorBarcodeDetailQuery.list();

			if (CollectionUtils.isNotEmpty(vendorBarcodeDetails)) {

                //TODO: fill vendorBarcodeItemInfo here ....

				LOGGER.debug("Found item code [{}] to cache for vendor-barcode [{}]-[{}]", vendorBarcodeItemInfo.getItemCode(), barcode, vendorId);
			} else {
				LOGGER.debug("No data found while retrieving barcode [{}] information for vendor [{}]", barcode, vendorId);
			}
			session.close();
		}
		return vendorBarcodeItemInfo;
	}

}
