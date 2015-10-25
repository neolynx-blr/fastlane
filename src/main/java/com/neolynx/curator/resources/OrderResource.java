/**
 * 
 */
package com.neolynx.curator.resources;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynx.common.model.order.CartDetail;
import com.neolynx.common.model.order.ClosureRequest;
import com.neolynx.common.model.order.Response;
import com.neolynx.curator.manager.OrderProcessor;

/**
 * Created by nitesh.garg on Oct 22, 2015
 *
 */


@Path("/curator/order")
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {
	
	private final OrderProcessor processor;

	public OrderResource(OrderProcessor processor) {
		super();
		this.processor = processor;
	}
	
	@Path("/create")
	@POST
	@UnitOfWork
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createOrder(CartDetail request) {
		System.out.println("Request received from vendor::" + request.getVendorId());
		return processor.createOrder(request);
	}

	@Path("/update")
	@POST
	@UnitOfWork
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateOrder(CartDetail request) {
		System.out.println("Request received from vendor::" + request.getVendorId());
		return processor.updateOrder(request);
	}
	
	@Path("/close")
	@POST
	@UnitOfWork
	@Consumes(MediaType.APPLICATION_JSON)
	public Response completeOrder(ClosureRequest request) {
		System.out.println("Request received from vendor::" + request.getOrderId());
		return processor.completeOrder(request);
	}
	

}
