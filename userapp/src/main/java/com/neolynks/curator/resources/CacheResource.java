package com.neolynks.curator.resources;

import com.neolynks.api.common.Response;
import com.neolynks.curator.cache.inventory.model.CacheDetail;
import com.neolynks.curator.manager.CacheEvaluator;
import io.dropwizard.hibernate.UnitOfWork;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * This class is meant for all REST interfaces that will be invoked to check any
 * status of caches serving the user requests & other backend processing
 * 
 * Created by nitesh.garg on Oct 3, 2015
 */

@Path("/curator/cache")
@Produces(MediaType.APPLICATION_JSON)
public class CacheResource {

	private final CacheEvaluator cacheEvaluator;

	public CacheResource(CacheEvaluator cacheEvaluator) {
		super();
		this.cacheEvaluator = cacheEvaluator;
	}

	@Path("/{vendorId}")
	@GET
	@RolesAllowed("Administrator, Analyst")
	@UnitOfWork
	public Response<List<CacheDetail>> getVendorCacheDetails(@PathParam(value = "vendorId") Long vendorId) {
		return this.cacheEvaluator.getVendorCacheDetails(vendorId);
	}

	@Path("/{vendorId}/current")
	@GET
	@RolesAllowed("Administrator, Analyst")
	@UnitOfWork
	public Response<List<CacheDetail>> getVendorCurrentInventoryCacheDetails( @PathParam(value = "vendorId") Long vendorId) {
		return this.cacheEvaluator.getCurrentInventoryCacheDetails(vendorId);
	}

	@Path("/recentItems")
	@GET
	@RolesAllowed("Administrator, Analyst")
	@UnitOfWork
	public Response<List<CacheDetail>> getVendorCacheDetails() {
		return this.cacheEvaluator.getRecentItemsCacheDetails();
	}
}
