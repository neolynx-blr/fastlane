package com.neolynx.curator.task;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.lifecycle.Managed;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.neolynx.common.model.InventoryResponse;
import com.neolynx.curator.manager.CacheCurator;
import com.neolynx.curator.manager.CacheSetup;

/**
 * Created by nitesh.garg on 06-Sep-2015
 */
public class DataLoaderJob implements Managed {

	private final CacheCurator cacheCurator;
	private final LoadingCache<Long, Long> vendorVersionCache;
	private final LoadingCache<String, InventoryResponse> recentItemsCache;
	private final LoadingCache<String, InventoryResponse> differentialInventoryCache;
	
	static Logger LOGGER = LoggerFactory.getLogger(DataLoaderJob.class);

	final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("DataLoader-%d").setDaemon(true)
			.build();
	final ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);

	public DataLoaderJob(LoadingCache<String, InventoryResponse> differentialInventoryCache,
			LoadingCache<Long, Long> vendorVersionCache, LoadingCache<String, InventoryResponse> recentItemsCache, CacheCurator cacheCurator) {
		this.cacheCurator = cacheCurator;
		this.recentItemsCache = recentItemsCache;
		this.vendorVersionCache = vendorVersionCache;
		this.differentialInventoryCache = differentialInventoryCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.dropwizard.lifecycle.Managed#start()
	 */
	@Override
	@UnitOfWork
	public void start() throws Exception {
		LOGGER.debug(
				"Starting up data-loader job with initial sizes of caches as [{}] for differential cache and [{}] for vendor-version cache",
				this.differentialInventoryCache.size(), this.vendorVersionCache.size());
		executorService.execute(new CacheSetup(this.differentialInventoryCache, this.vendorVersionCache, this.recentItemsCache,
				this.cacheCurator));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.dropwizard.lifecycle.Managed#stop()
	 */
	@Override
	public void stop() throws Exception {

		LOGGER.info("Shutting down the executor service...");
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);
		LOGGER.info("Completed.");

	}

}
