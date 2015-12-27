package com.neolynks.curator.resources;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynks.common.model.client.InventoryInfo;

/**
 * Created by nitesh.garg on Dec 25, 2015
 *
 * This class is meant to support all REST based resources for merchant worker
 * app which is helping the consumer complete the shopping work flow. These
 * operations includes, 
 * 
 * 1. Sign into or out-off the queue of active workers for a given vendor (IP based authentication). Additionally, no more queue allocation, or reject any carts eing allocated.
 * 2. Get set of carts (and it's details) to start working on (Response: Some user/cart identifiers, item details for the cart) 
 * 3. Update cart status back to server (Request: cart identifier, and status, Response: Any delta is available at that stage) 
 * 4. Request for adding/removing items from the cart post the cart was closed by the user 
 * 5. Request for user exiting the store post the payment has been completed 
 * 6. Continuous poll for latest on all cart processed in last 2-3 hours to get to know when payment is completed, and/or if stuck anywhere
 *
 */

@Path("/curator/worker/")
@Produces(MediaType.APPLICATION_JSON)
public class WorkerResource {

	
	@Path("/{vendorId}/current/all")
	@GET
	@UnitOfWork
	public InventoryInfo getLatestInventory(@PathParam(value = "vendorId") Long vendorId) {
		return null;
	}
	
	
}
