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
 * Simply creates a cache of latest known data version for all vendors in the
 * system
 * 
 * Created by nitesh.garg on 07-Sep-2015
 */
public class VendorVersionLoader extends CacheLoader<Long, Long> {

	private SessionFactory sessionFactory;
	static Logger LOGGER = LoggerFactory.getLogger(VendorVersionLoader.class);

	public VendorVersionLoader(SessionFactory sessionFactory) {
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
	public Long load(Long vendorId) throws Exception {

		Long versionId = null;
		if (vendorId == null) {
			LOGGER.debug("Tried loading latest version of NULL vendor-id, obviously failed.");
		} else {

			Session session = sessionFactory.openSession();

			Query vendorDetailQuery = session
					.createSQLQuery(" select vvd.* from vendor_version_detail vvd where vendor_id = :vendorId ")
					.addEntity("vendor_version_detail", VendorVersionDetail.class).setParameter("vendorId", vendorId);

			List<VendorVersionDetail> versionIds = vendorDetailQuery.list();

			if (CollectionUtils.isNotEmpty(versionIds)) {
				versionId = (Long) versionIds.get(0).getLatestSyncedVersionId();
				LOGGER.debug("Latest version [{}] is being cached against the vendor [{}]", versionId, vendorId);
			} else {
				LOGGER.debug("No data found while retrieving latest version for vendor [{}]", vendorId);
			}
			session.close();
		}
		return versionId;
	}

}
