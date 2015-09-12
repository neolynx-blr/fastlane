package com.example.helloworld.cache;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.VendorVersionDetail;
import com.example.helloworld.core.VendorVersionDifferential;
import com.google.common.cache.LoadingCache;

/**
 * Used for initial cache data loading. Simply get all data rows in the
 * vendor_version_detail and vendor_version_differential tables and call the get
 * on cache which loads the data if that is absent.
 * 
 * Created by nitesh.garg on 07-Sep-2015
 */
public class InitialSetup {

	private final SessionFactory sessionFactory;
	private final LoadingCache<Long, Long> vendorVersionCache;
	private final LoadingCache<String, InventoryResponse> differentialInventoryCache;

	static Logger LOGGER = LoggerFactory.getLogger(InitialSetup.class);

	public InitialSetup(SessionFactory sessionFactory,
			LoadingCache<String, InventoryResponse> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache) {
		super();
		this.sessionFactory = sessionFactory;
		this.differentialInventoryCache = differentialInventoryCache;
		this.vendorVersionCache = vendorVersionCache;
	}

	@SuppressWarnings("unchecked")
	public void setupInitialCaches() {

		Session session = sessionFactory.openSession();

		Query diffQuery = session.createSQLQuery(
				"select vvd.* from vendor_version_differential vvd order by last_modified_on ").addEntity(
				"vendor_version_differential", VendorVersionDifferential.class);

		List<VendorVersionDifferential> vendorVersionDifferentials = diffQuery.list();
		LOGGER.info("As part of the initial cache setup, trying to add [{}] entries of differential data",
				vendorVersionDifferentials.size());

		for (VendorVersionDifferential diffInstance : vendorVersionDifferentials) {
			try {
				
				String key = diffInstance.getVendorId() + "-" + diffInstance.getVersionId();
				this.differentialInventoryCache.get(key);
				LOGGER.info("Successfully added cache for vendor-version, [{}-{}]", diffInstance.getVendorId(),
						diffInstance.getVersionId());
			} catch (Exception e) {
				LOGGER.error("Error {" + e.getMessage() + "} occurred while loading combination {"
						+ diffInstance.getVendorId() + ":" + diffInstance.getVersionId() + "} into the cache.");
				e.printStackTrace();
			}
		}

		Query vendorDetailQuery = session.createSQLQuery(" select vvd.* from vendor_version_detail vvd ").addEntity(
				"vendor_version_detail", VendorVersionDetail.class);

		List<VendorVersionDetail> vendorVersionDetails = vendorDetailQuery.list();

		for (VendorVersionDetail instance : vendorVersionDetails) {
			Long vendorId = instance.getVendorId();
			try {
				this.vendorVersionCache.get(instance.getVendorId());
				LOGGER.info("Adding version [{}] as the latest one for vendor [{}]",
						this.vendorVersionCache.getIfPresent(vendorId), vendorId);
			} catch (ExecutionException e) {
				LOGGER.error("Error {" + e.getMessage() + "} occurred while loading version for vendor {" + vendorId
						+ "} into the cache.");
				e.printStackTrace();
			}

		}

		session.close();

	}

}
