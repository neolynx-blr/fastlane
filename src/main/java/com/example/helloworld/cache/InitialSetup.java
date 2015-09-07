package com.example.helloworld.cache;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.VendorVersionDetail;
import com.example.helloworld.core.VendorVersionDifferential;
import com.google.common.cache.LoadingCache;

/**
 * Created by nitesh.garg on 07-Sep-2015
 */
public class InitialSetup {

	private final SessionFactory sessionFactory;
	private final LoadingCache<String, InventoryResponse> differentialInventoryCache;
	private final LoadingCache<Long, Long> vendorVersionCache;

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
		System.out.println("As part of the initial cache setup, trying to add {" + vendorVersionDifferentials.size()
				+ "} entries.");

		for (VendorVersionDifferential diffInstance : vendorVersionDifferentials) {
			try {
				this.differentialInventoryCache.get(diffInstance.getVendorId() + "-" + diffInstance.getVersionId());
				System.out.println("Successfully added cache for {" + diffInstance.getVendorId() + "-"
						+ diffInstance.getVersionId() + "}.");
			} catch (Exception e) {
				System.out.println("Error {" + e.getMessage() + "} occurred while loading combination {"
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
				System.out.println("Adding version {" + this.vendorVersionCache.getIfPresent(vendorId)
						+ "} as the latest one for vendor {" + vendorId + "}");
			} catch (ExecutionException e) {
				System.out.println("Error {" + e.getMessage() + "} occurred while loading version for vendor {"
						+ vendorId + "} into the cache.");
				e.printStackTrace();
			}

		}

	}

}
