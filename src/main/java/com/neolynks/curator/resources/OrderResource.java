package com.neolynks.curator.resources;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynks.curator.manager.OrderProcessor;
import com.neolynks.common.model.order.CartRequest;
import com.neolynks.common.model.order.ClosureRequest;
import com.neolynks.common.model.order.Response;

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
	public Response createOrder(CartRequest request) {
		System.out.println("Request received from vendor::" + request.getVendorId());
		return processor.createOrder(request);
	}

	@Path("/update")
	@POST
	@UnitOfWork
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateOrder(CartRequest request) {
		System.out.println("Request received from vendor::" + request.getVendorId());
		return processor.updateOrder(request);
	}

	@Path("/close/instore")
	@POST
	@UnitOfWork
	@Consumes(MediaType.APPLICATION_JSON)
	public Response closeOrderInStore(ClosureRequest request) {
		System.out.println("Request received from vendor::" + request.getOrderId());
		return processor.completeInStoreProcessing(request);
	}

	@Path("/close/delivery")
	@POST
	@UnitOfWork
	@Consumes(MediaType.APPLICATION_JSON)
	public Response closeOrderDelivery(ClosureRequest request) {
		System.out.println("Request received from vendor::" + request.getOrderId());
		return processor.completeDeliveryProcessing(request);
	}

}
