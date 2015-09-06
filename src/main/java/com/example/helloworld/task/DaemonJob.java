package com.example.helloworld.task;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.lifecycle.Managed;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.hibernate.SessionFactory;

import com.example.helloworld.manager.InventorySetup;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Created by nitesh.garg on 04-Sep-2015
 */
public class DaemonJob implements Managed {
	
	private SessionFactory sessionFactory; 
	
	final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("DaemonJob-%d").setDaemon(true).build();
	final ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);
	
	public DaemonJob(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.dropwizard.lifecycle.Managed#start()
	 */
	@Override
	@UnitOfWork
	public void start() throws Exception {
		executorService.execute(new InventorySetup(sessionFactory));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.dropwizard.lifecycle.Managed#stop()
	 */
	@Override
	public void stop() throws Exception {
		
		System.out.println("Shutting down the executor service...");
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);
		System.out.println("Completed.");
		
	}

}
