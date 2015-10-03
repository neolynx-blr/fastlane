package com.neolynx.curator.resources;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynx.common.model.InventoryResponse;
import com.neolynx.curator.manager.InventoryEvaluator;

/**
 * This class is meant for all REST interfaces that will be involved by the user from Mobile App
 * 
 * Created by nitesh.garg on Oct 3, 2015
 */

@Path("/fastlane/inventory")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
	
	private final InventoryEvaluator inventoryEvaluator;
	
	public UserResource(InventoryEvaluator inventoryEvaluator) {
		super();
		this.inventoryEvaluator = inventoryEvaluator;
	}

	@Path("/{vendorId}/{versionId}")
	@GET
	@UnitOfWork
	public InventoryResponse getInventoryDifferential(@PathParam(value = "vendorId") Long vendorId,
			@PathParam(value = "versionId") Long dataVersionId) {
		return this.inventoryEvaluator.getInventoryDifferential(vendorId, dataVersionId);
	}
	
	@Path("/{vendorId}")
	@GET
	@UnitOfWork
	public InventoryResponse getLatestInventory(@PathParam(value = "vendorId") Long vendorId) {
		return this.inventoryEvaluator.getLatestInventory(vendorId);
	}

}