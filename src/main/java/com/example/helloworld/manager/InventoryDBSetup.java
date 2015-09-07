package com.example.helloworld.manager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.example.helloworld.core.InventoryResponse;
import com.google.common.cache.LoadingCache;

/**
 * Created by nitesh.garg on 04-Sep-2015
 * 
 * Intention of this class is to run as a background thread and continue to look
 * for new arriving inventory. Once something is found, make the updates to the
 * required data sets used for serving. Note that, inventory_master stores all
 * the incoming inventory data. From there it gets copied to product_master,
 * vendor_item_master and vendor_item_differential.
 * 
 * inventory_master (Global inventory of all time) product_master (Global
 * product data, vendor specific) vendor_item_master (Current latest vendor
 * specific inventory)
 * 
 */

public class InventoryDBSetup implements Runnable {

	private SessionFactory sessionFactory;
	private final LoadingCache<Long, Long> vendorVersionCache;
	private final LoadingCache<String, InventoryResponse> differentialInventoryCache;

	public InventoryDBSetup(SessionFactory sessionFactory,
			LoadingCache<String, InventoryResponse> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache) {
		super();
		this.sessionFactory = sessionFactory;
		this.vendorVersionCache = vendorVersionCache;
		this.differentialInventoryCache = differentialInventoryCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		// Keeping running for ever in the background looking for new inventory
		while (true) {

			try {

				// Check for new inventory every 'X' seconds.
				Thread.sleep(20000);

				Session session = sessionFactory.openSession();
				
				InventoryCurator curator = new InventoryCurator(sessionFactory);
				curator.processNewInventory();
				curator.processVendorVersionMeta(this.differentialInventoryCache, this.vendorVersionCache);

				session.close();

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// Eat away to make sure infinite loop continues for data refresh
			}

		}

	}

}
