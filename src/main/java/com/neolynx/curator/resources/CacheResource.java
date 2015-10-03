package com.neolynx.curator.resources;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynx.common.model.CacheDetail;
import com.neolynx.curator.manager.CacheEvaluator;

/**
 * This class is meant for all REST interfaces that will be invoked to check any
 * status of caches serving the user requests & other backend processing
 * 
 * Created by nitesh.garg on Oct 3, 2015
 */

@Path("/fastlane/cache")
@Produces(MediaType.APPLICATION_JSON)
public class CacheResource {

	private final CacheEvaluator cacheEvaluator;

	public CacheResource(CacheEvaluator cacheEvaluator) {
		super();
		this.cacheEvaluator = cacheEvaluator;
	}

	@Path("/cache/{vendorId}")
	@GET
	@UnitOfWork
	public List<CacheDetail> getCacheDetails(@PathParam(value = "vendorId") Long vendorId) {
		return this.cacheEvaluator.getCacheDetails(vendorId);
	}

}
