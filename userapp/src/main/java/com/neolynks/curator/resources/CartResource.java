package com.neolynks.curator.resources;

import com.neolynks.api.common.Response;
import com.neolynks.api.userapp.CartRequest;
import com.neolynks.api.userapp.ClosureRequest;
import com.neolynks.curator.annotation.UserContextRequired;
import io.dropwizard.hibernate.UnitOfWork;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynks.curator.manager.CartHandler;
import com.neolynks.curator.manager.OrderProcessor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Created by nitesh.garg on Dec 26, 2015
 *
 */

@Slf4j
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
    @UserContextRequired
    public Response<String> initialiseCart() {
        return this.cartHandler.initializeCart();
    }

    @Path("/{id}/set")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @UnitOfWork
    @UserContextRequired
    public Response<Void> setToCart(@PathParam(value = "id") String cartId,
                                  @Valid CartRequest cartRequest) {
        return this.cartHandler.setToCart(cartId, cartRequest.getItemCount());
    }

    @Path("/{id}/set/status/{id}")
    @POST
    @UnitOfWork
    @UserContextRequired
    public Response<Void> setCartStatus(@PathParam(value = "id") String cartId,
                                      @PathParam(value = "id") Integer statusId) {
        return this.cartHandler.setCartStatus(cartId, statusId);
    }
}