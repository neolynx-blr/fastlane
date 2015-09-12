package com.example.helloworld.resources;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.manager.InventoryEvaluator;
import com.example.helloworld.manager.InventoryLoader;

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

	@Path("/{vendorId}")
	@GET
	@UnitOfWork
	public InventoryResponse getLatestInventory(@PathParam(value = "vendorId") Long vendorId) {
		return evaluator.getLatestInventory(vendorId);
	}

	@Path("/load")
	@POST
	@UnitOfWork
	@Consumes(MediaType.APPLICATION_JSON)
	public void postInventoryUpdate(InventoryResponse inventoryResponse) {

		System.out.println("Request received from vendor::"+inventoryResponse.getVendorId());
		loader.loadNewInventory(inventoryResponse);

	}

}
