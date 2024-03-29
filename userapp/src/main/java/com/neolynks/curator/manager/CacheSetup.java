package com.neolynks.curator.manager;

import io.dropwizard.hibernate.UnitOfWork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynks.common.model.client.InventoryInfo;

/**
 * Created by nitesh.garg on 06-Sep-2015
 */
public class CacheSetup implements Runnable {

	private final CacheCurator cacheCurator;
	private final LoadingCache<Long, Long> vendorVersionCache;
	private final LoadingCache<String, InventoryInfo> recentItemsCache;
	private final LoadingCache<String, InventoryInfo> differentialInventoryCache;

	static Logger LOGGER = LoggerFactory.getLogger(CacheSetup.class);

	public CacheSetup(LoadingCache<String, InventoryInfo> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache, LoadingCache<String, InventoryInfo> recentItemsCache, CacheCurator cacheCurator) {
		super();
		this.cacheCurator = cacheCurator;
		this.recentItemsCache = recentItemsCache;
		this.vendorVersionCache = vendorVersionCache;
		this.differentialInventoryCache = differentialInventoryCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	@UnitOfWork
	public void run() {

		/*
		 * TODO: Right now all the cache are thoroughly checked and updated if
		 * needed. For optimization, we can keep a time stamp of last update and
		 * only look into these against the last_modified column of the tables.
		 */
		// Keeping running for ever in the background looking for new inventory
		while (true) {

			try {

				// Check for new inventory every 'X' seconds.
				Thread.sleep(10000);

				LOGGER.debug("Wokeup with differential cache-size of [{}], vendor-barcode cache-size [{}] and vendor-version cache-size of [{}]",
						this.differentialInventoryCache.size(), this.recentItemsCache.size(), this.vendorVersionCache.size());

				this.cacheCurator.processVendorVersionCache();
				this.cacheCurator.processCurrentInventoryCache();
				this.cacheCurator.processRecentItemRecordsCache();
				this.cacheCurator.processDifferentialInventoryCache();

				LOGGER.debug(
						"Leaving with cache-size of [{}] entries in differential, [{}] entries in vendor-barcode cache and [{}] in vendor-version caches",
						this.differentialInventoryCache.size(), this.recentItemsCache.size(), this.vendorVersionCache.size());

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
