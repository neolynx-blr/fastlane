package com.neolynks.curator.resources;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynks.common.model.client.InventoryInfo;
import com.neolynks.curator.manager.InventoryEvaluator;

/**
 * This class is meant for all REST interfaces that will be involved by the user from Mobile App
 * 
 * Created by nitesh.garg on Oct 3, 2015
 */

@Path("/curator/inventory")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
	
	private final InventoryEvaluator inventoryEvaluator;
	
	public UserResource(InventoryEvaluator inventoryEvaluator) {
		super();
		this.inventoryEvaluator = inventoryEvaluator;
	}

	@Path("/{vendorId}/current/all")
	@GET
	@UnitOfWork
	public InventoryInfo getLatestInventory(@PathParam(value = "vendorId") Long vendorId) {
		return this.inventoryEvaluator.getLatestInventory(vendorId);
	}

	@Path("/{vendorId}/current/{barcode}")
	@GET
	@UnitOfWork
	public InventoryInfo getLatestItemRecord(@PathParam(value = "vendorId") Long vendorId, @PathParam(value = "barcode") Long barcode) {
		return this.inventoryEvaluator.getLatestItemForVendorBarcode(vendorId, barcode);
	}

	@Path("/{vendorId}/{versionId}/all")
	@GET
	@UnitOfWork
	public InventoryInfo getInventoryDifferential(@PathParam(value = "vendorId") Long vendorId,
			@PathParam(value = "versionId") Long dataVersionId) {
		return this.inventoryEvaluator.getInventoryDifferential(vendorId, dataVersionId);
	}
	
}
