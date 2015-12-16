package com.neolynks.curator.cache;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheLoader;
import com.neolynks.curator.core.VendorVersionDetail;

/**
 * Created by nitesh.garg on Nov 1, 2015
 *
 */
public class CurrentInventoryLoader extends CacheLoader<Long, String> {

	private SessionFactory sessionFactory;
	static Logger LOGGER = LoggerFactory.getLogger(CurrentInventoryLoader.class);

	public CurrentInventoryLoader(SessionFactory sessionFactory) {
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
	public String load(Long vendorId) throws Exception {
		
		String currentInventory = null;
		LOGGER.info("Request received for refreshing current inventory cache for vendor [{}]", vendorId);

		if (vendorId == null) {
			LOGGER.debug("Tried loading latest version of NULL vendor-id, obviously failed.");
		} else {

			Session session = sessionFactory.openSession();

			Query vendorDetailQuery = session
					.createSQLQuery(" select vvd.* from vendor_version_detail vvd where vendor_id = :vendorId ")
					.addEntity("vendor_version_detail", VendorVersionDetail.class).setParameter("vendorId", vendorId);

			List<VendorVersionDetail> vendorVersionDetailList = vendorDetailQuery.list();

			if (CollectionUtils.isNotEmpty(vendorVersionDetailList)) {

				VendorVersionDetail vendorVersionDetail = vendorVersionDetailList.get(0);

				currentInventory = (String) vendorVersionDetail.getCurrentInventory();
				LOGGER.debug(
						"Latest version [{}] is being cached against the vendor [{}] containing [{}] Added, [{}] Updated and [{}] Deleted items.",
						vendorVersionDetail.getLatestSyncedVersionId(), vendorId);
			} else {
				LOGGER.debug("No data found while retrieving latest version for vendor [{}]", vendorId);
			}
			session.close();
		}

		return currentInventory;
	}

}
