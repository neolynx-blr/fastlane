package com.neolynx.curator.resources;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynx.common.model.CacheDetail;
import com.neolynx.common.model.InventoryRequest;
import com.neolynx.common.model.InventoryResponse;
import com.neolynx.common.model.ResponseAudit;
import com.neolynx.curator.manager.InventoryEvaluator;
import com.neolynx.curator.manager.InventoryLoader;

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
public class InventoryResource {

	private final InventoryLoader loader;
	private final InventoryEvaluator evaluator;

	public InventoryResource(InventoryEvaluator evaluator, InventoryLoader loader) {
		super();
		this.loader = loader;
		this.evaluator = evaluator;
	}

	@Path("/{vendorId}/{versionId}")
	@GET
	@UnitOfWork
	public InventoryResponse getInventoryDifferential(@PathParam(value = "vendorId") Long vendorId,
			@PathParam(value = "versionId") Long dataVersionId) {
		return evaluator.getInventoryDifferential(vendorId, dataVersionId);
	}

	@Path("/cache/{vendorId}")
	@GET
	@UnitOfWork
	public List<CacheDetail> getCacheDetails(@PathParam(value = "vendorId") Long vendorId) {
		return evaluator.getCacheDetails(vendorId);
	}
	
	@Path("/{vendorId}")
	@GET
	@UnitOfWork
	public InventoryResponse getLatestInventory(@PathParam(value = "vendorId") Long vendorId) {
		return evaluator.getLatestInventory(vendorId);
	}
	
	@Path("/{vendorId}/lastSyncId")
	@GET
	@UnitOfWork
	public InventoryResponse getLastKnownSyncId(@PathParam(value = "vendorId") Long vendorId) {
		// TODO
		return evaluator.getLatestInventory(vendorId);
	}
	
	@Path("/{vendorId}/freshLoad")
	@GET
	@UnitOfWork
	public ResponseAudit loadFreshInventoryDetailsFromCSV(@PathParam(value = "vendorId") Long vendorId) {
		return loader.freshInventoryLoad(vendorId);
	}
	
	@Path("/load")
	@POST
	@UnitOfWork
	@Consumes(MediaType.APPLICATION_JSON)
	public ResponseAudit postInventoryUpdate(InventoryRequest request) {
		System.out.println("Request received from vendor::"+request.getVendorId());
		return loader.loadNewInventory(request);

	}

}
