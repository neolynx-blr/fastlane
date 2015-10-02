package com.neolynx.vendor.job;

import io.dropwizard.lifecycle.Managed;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.neolynx.vendor.manager.InventorySyncCron;
import com.neolynx.vendor.model.CurationConfig;

/**
 * Created by nitesh.garg on 12-Sep-2015
 */

public class InventorySync implements Managed {
	
	static Logger LOGGER = LoggerFactory.getLogger(InventorySync.class);
	
	private final CurationConfig curationConfig;
	
	public InventorySync(CurationConfig curationConfig) {
		super();
		this.curationConfig = curationConfig;
	}

	final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("VendorInvLoader-%d").setDaemon(true).build();
	final ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);

	
	/* (non-Javadoc)
	 * @see io.dropwizard.lifecycle.Managed#start()
	 */
	@Override
	public void start() throws Exception {
		LOGGER.debug("Starting vendor side inventory loader process with configuration as [{}]", this.curationConfig.toString());
		executorService.execute(new InventorySyncCron(this.curationConfig));
	}

	/* (non-Javadoc)
	 * @see io.dropwizard.lifecycle.Managed#stop()
	 */
	@Override
	public void stop() throws Exception {
		
		LOGGER.info("Shutting down the executor service (Vendor side data loading)...");
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);
		LOGGER.info("Completed terminating the executor service for loading data from vendor side.");
		
	}

}
