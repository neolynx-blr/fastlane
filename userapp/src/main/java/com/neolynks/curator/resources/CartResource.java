package com.neolynks.curator.resources;

import com.neolynks.api.common.ErrorCode;
import com.neolynks.api.common.Response;
import com.neolynks.api.userapp.CartRequest;
import com.neolynks.curator.annotation.UserContextRequired;
import com.neolynks.curator.exception.InvalidCartIdException;
import io.dropwizard.hibernate.UnitOfWork;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynks.curator.manager.CartHandler;
import com.neolynks.curator.manager.OrderProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by nitesh.garg on Dec 26, 2015
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
        String cartId = this.cartHandler.initializeCart();
        Response<String> successResponse =  Response.getSuccessResponse(cartId);
        return successResponse;
    }

    @Path("/{id}/set")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @UnitOfWork
    @UserContextRequired
    public Response<Void> setToCart(@PathParam(value = "id")
                                    @Nonnull String cartId,
                                    @Valid CartRequest cartRequest) {
        try {
            this.cartHandler.setToCart(cartId, cartRequest.getItemCount());
            Response successResponse = Response.getSuccessResponse(null);
            return successResponse;
        }catch (InvalidCartIdException cim){
            Response failureResponse = Response.getFailureResponse(ErrorCode.MISSING_ORDER_ID);
            return failureResponse;
        }
    }

    @Path("/{id}/set/status/{id}")
    @POST
    @UnitOfWork
    @UserContextRequired
    public Response<Void> setCartStatus(@PathParam(value = "id") String cartId,
                                        @PathParam(value = "id") Integer statusId) {
        try {
            this.cartHandler.setCartStatus(cartId, statusId);
            Response successResponse = Response.getSuccessResponse(null);
            return successResponse;
        }catch (InvalidCartIdException cim){
            Response failureResponse = Response.getFailureResponse(ErrorCode.MISSING_ORDER_ID);
            return failureResponse;
        }
    }
}