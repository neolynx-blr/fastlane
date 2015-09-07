package com.example.helloworld.cache;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.example.helloworld.core.VendorVersionDetail;
import com.google.common.cache.CacheLoader;

/**
 * Created by nitesh.garg on 07-Sep-2015
 */
public class VendorVersionLoader extends CacheLoader<Long, Long> {

	private SessionFactory sessionFactory;

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
		Session session = sessionFactory.openSession();
		Query vendorDetailQuery = session
				.createSQLQuery(
						" select vvd.* from vendor_version_detail vvd where vendor_id = :vendorId ")
				.addEntity("vendor_version_detail", VendorVersionDetail.class).setParameter("vendorId", vendorId);

		List<VendorVersionDetail> versionIds = vendorDetailQuery.list();

		if (!versionIds.isEmpty()) {
			versionId = (Long) versionIds.get(0).getLatestSyncedVersionId();
			System.out.println("Found version {" + versionId + "} as the latest one for vendor {" + vendorId + "}");
		}
		session.close();
		return versionId;
	}

}
