package com.neolynks.curator.resources;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynks.common.model.cart.CartResponse;
import com.neolynks.common.model.order.CartPreview;
import com.neolynks.common.model.order.CartRequest;
import com.neolynks.common.model.order.ClosureRequest;
import com.neolynks.common.model.order.Response;
import com.neolynks.curator.manager.CartHandler;
import com.neolynks.curator.manager.OrderProcessor;

/**
 * Created by nitesh.garg on Dec 26, 2015
 *
 */

@Path("/curator/cart")
@Produces(MediaType.APPLICATION_JSON)
public class CartResource {

    private final CartHandler cartHandler;
    private final OrderProcessor processor;

    /**
     * @param cartEvaluator
     */
    public CartResource(CartHandler cartEvaluator, OrderProcessor processor) {
        super();
        this.cartHandler = cartEvaluator;
        this.processor = processor;
    }

    @Path("/init")
    @GET
    @UnitOfWork
    public CartResponse initialiseCart(@HeaderParam(value = "vendorId") Long vendorId,
                                       @HeaderParam(value = "userId") Long userId) {
        return this.cartHandler.initializeCart(vendorId, userId);
    }

    @Path("/{id}/set/{barcode}/{count}")
    @GET
    @UnitOfWork
    public CartResponse setToCart(@PathParam(value = "id") Long cartId,
                                  @PathParam(value = "barcode") Long barcode, @PathParam(value = "count") Integer count) {
        return this.cartHandler.setToCart(cartId, barcode, count);
    }

    @Path("/{id}/set/status/{id}")
    @GET
    @UnitOfWork
    public CartResponse setCartStatus(@PathParam(value = "id") Long cartId,
                                      @PathParam(value = "id") Integer statusId) {
        return this.cartHandler.setCartStatus(cartId, statusId);
    }

    @Path("/init/set")
    @POST
    @UnitOfWork
    public CartResponse initialiseNSetCart(CartPreview cartPreview) {
        return this.cartHandler.initializeNSetCart(cartPreview);
    }

    /**
     *  Close this operation because tracking w.r.t. worker app is complete lost, need to check when it'll make sense.
     * @param cartId
     * @param cartPreview
     * @return
     */
	
	/*
	@Path("{id}/set")
	@POST
	@UnitOfWork
	public CartResponse setCartContent(@PathParam(value = "id") Long cartId, CartPreview cartPreview) {
		return this.cartHandler.setCartContent(cartId, cartPreview);
	}
	*/

    @Path("/{id}/order")
    @POST
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrder(CartRequest request) {
        System.out.println("Request received from vendor::" + request.getVendorId());
        return processor.createOrder(request);
    }

    @Path("/{id}/update")
    @POST
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateOrder(CartRequest request) {
        System.out.println("Request received from vendor::" + request.getVendorId());
        return processor.updateOrder(request);
    }

    @Path("{id}/close/instore")
    @POST
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    public Response closeOrderInStore(ClosureRequest request) {
        System.out.println("Request received from vendor::" + request.getOrderId());
        return processor.completeInStoreProcessing(request);
    }

    @Path("{id}/close/delivery")
    @POST
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    public Response closeOrderDelivery(ClosureRequest request) {
        System.out.println("Request received from vendor::" + request.getOrderId());
        return processor.completeDeliveryProcessing(request);
    }

}