package com.neolynx.curator.resources;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynx.common.model.BaseResponse;
import com.neolynx.common.model.InventoryRequest;
import com.neolynx.common.model.InventoryResponse;
import com.neolynx.common.model.ResponseAudit;
import com.neolynx.curator.manager.InventoryEvaluator;
import com.neolynx.curator.manager.InventoryLoader;

/**
 * This class is meant for all REST interfaces that will be involved for
 * handling vendor side inventory operations
 * 
 * Created by nitesh.garg on Oct 3, 2015
 */

@Path("/fastlane/vendor")
@Produces(MediaType.APPLICATION_JSON)
public class VendorResource {

	private final InventoryLoader loader;
	private final InventoryEvaluator evaluator;

	public VendorResource(InventoryEvaluator evaluator, InventoryLoader loader) {
		super();
		this.loader = loader;
		this.evaluator = evaluator;
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
	public BaseResponse loadFreshInventoryDetailsFromCSV(@PathParam(value = "vendorId") Long vendorId) {
		return loader.freshInventoryLoad(vendorId);
	}

	@Path("/load")
	@POST
	@UnitOfWork
	@Consumes(MediaType.APPLICATION_JSON)
	public ResponseAudit postInventoryUpdate(InventoryRequest request) {
		System.out.println("Request received from vendor::" + request.getVendorId());
		return loader.loadNewInventory(request);

	}

}
