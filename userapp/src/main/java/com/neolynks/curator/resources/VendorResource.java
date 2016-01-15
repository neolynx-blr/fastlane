package com.neolynks.curator.resources;

import com.neolynks.api.common.Response;
import com.neolynks.api.common.inventory.InventoryInfo;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynks.common.model.client.InventoryInfo;
import com.neolynks.curator.core.User;
import com.neolynks.curator.manager.InventoryEvaluator;
import com.neolynks.curator.manager.InventoryLoader;
import com.neolynks.common.model.BaseResponse;
import com.neolynks.common.model.InventoryRequest;
import com.neolynks.common.model.ResponseAudit;

/**
 * This class is meant for all REST interfaces that will be involved for
 * handling vendor side inventory operations
 * 
 * Created by nitesh.garg on Oct 3, 2015
 */

@Path("/curator/vendor")
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
	@RolesAllowed("Administrator, Vendor")
	@UnitOfWork
	public Response<InventoryInfo> getLastKnownSyncId(@Auth User user, @PathParam(value = "vendorId") Long vendorId) {
		// TODO
		return evaluator.getLatestInventory(vendorId);
	}

	@Path("/{vendorId}/freshLoad")
	@GET
	@RolesAllowed("Administrator")
	@UnitOfWork
	public BaseResponse loadFreshInventoryDetailsFromCSV(@Auth User user, @PathParam(value = "vendorId") Long vendorId) {
		return loader.freshInventoryLoad(vendorId);
	}

	@Path("/load")
	@POST
	@UnitOfWork
	@RolesAllowed("Administrator, Vendor")
	@Consumes(MediaType.APPLICATION_JSON)
	public ResponseAudit postInventoryUpdate(@Auth User user, InventoryRequest request) {
		System.out.println("Request received from vendor::" + request.getVendorId());
		return loader.loadNewInventory(request);

	}

}
