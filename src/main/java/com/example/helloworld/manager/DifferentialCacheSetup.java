package com.example.helloworld.manager;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.VendorVersionDifferential;
import com.google.common.cache.LoadingCache;

/**
 * Created by nitesh.garg on 07-Sep-2015
 */
public class DifferentialCacheSetup {

	private final SessionFactory sessionFactory;
	private final LoadingCache<String, InventoryResponse> differentialInventoryCache;
	
	public DifferentialCacheSetup(SessionFactory sessionFactory,
			LoadingCache<String, InventoryResponse> differentialInventoryCache) {
		super();
		this.sessionFactory = sessionFactory;
		this.differentialInventoryCache = differentialInventoryCache;
	}

	@SuppressWarnings("unchecked")
	public void setupInitialCache() {

		Session session = sessionFactory.openSession();

		Query diffQuery = session.createSQLQuery(
				"select vvd.* from vendor_version_differential vvd order by last_modified_on ").addEntity(
				"vendor_version_differential", VendorVersionDifferential.class);

		List<VendorVersionDifferential> vendorVersionDifferentials = diffQuery.list();
		System.out.println("As part of the initial cache setup, trung to add {" + vendorVersionDifferentials.size() + "} entries.");

		for (VendorVersionDifferential diffInstance : vendorVersionDifferentials) {
			try {
				this.differentialInventoryCache.get(diffInstance.getVendorId() + "-" + diffInstance.getVersionId());
				System.out.println("Successfully added cache for {" + diffInstance.getVendorId() + "-" + diffInstance.getVersionId() + "}.");
			} catch (Exception e) {
				System.out.println("Error {" + e.getMessage() + "} occurred while loading combination {"
						+ diffInstance.getVendorId() + ":" + diffInstance.getVersionId() + "} into the cache.");
				e.printStackTrace();
			}
		}

	}


}
