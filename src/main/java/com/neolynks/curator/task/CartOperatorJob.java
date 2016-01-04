package com.neolynks.curator.task;

import io.dropwizard.lifecycle.Managed;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.neolynks.curator.manager.CartOperator;
import com.neolynks.curator.model.Cart;
import com.neolynks.worker.manager.WorkerCartHandler;

/**
 * Created by nitesh.garg on Dec 29, 2015
 *
 */
public class CartOperatorJob implements Managed {
	
	static Logger LOGGER = LoggerFactory.getLogger(CartOperatorJob.class);
	
	private final LoadingCache<Long, Cart> cartCache;
	private final WorkerCartHandler workerCartHandler;
	
	/**
	 * @param cartCache
	 */
	public CartOperatorJob(LoadingCache<Long, Cart> cartCache, WorkerCartHandler workerCartHandler) {
		super();
		this.cartCache = cartCache;
		this.workerCartHandler = workerCartHandler;
	}

	final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("CartOperatorJob-%d").setDaemon(true).build();
	final ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);


	/* (non-Javadoc)
	 * @see io.dropwizard.lifecycle.Managed#start()
	 */
	@Override
	public void start() throws Exception {
		LOGGER.info("Starting the CartOperator executor service...");
		executorService.execute(new CartOperator(cartCache, workerCartHandler));
	}

	/* (non-Javadoc)
	 * @see io.dropwizard.lifecycle.Managed#stop()
	 */
	@Override
	public void stop() throws Exception {
		LOGGER.info("Shutting down the CartOperator executor service...");
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);
		LOGGER.info("Completed.");
	}

}
