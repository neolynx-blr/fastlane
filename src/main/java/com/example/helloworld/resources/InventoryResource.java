package com.example.helloworld.resources;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.manager.InventoryEvaluator;

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
public class InventoryResource {
	
	private final InventoryEvaluator evaluator;
	
    public InventoryResource(InventoryEvaluator evaluator) {
		super();
		this.evaluator = evaluator;
	}

	@Path("/{vendorId}/{versionId}")
    @GET
    @UnitOfWork
    public InventoryResponse getInventoryDifferential(@PathParam(value="vendorId") Long vendorId, @PathParam(value="dataVersionId") String dataVersionId) {
    	return evaluator.getInventoryDifferential(vendorId, dataVersionId);
    }
	
	@Path("/{vendorId}")
    @GET
    @UnitOfWork
    public InventoryResponse getLatestInventory(@PathParam(value="vendorId") Long vendorId) {
    	return evaluator.getLatestInventory(vendorId);
    }
	
}
