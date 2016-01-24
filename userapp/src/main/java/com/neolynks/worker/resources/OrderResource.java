package com.neolynks.worker.resources;

import com.neolynks.api.common.ErrorCode;
import com.neolynks.api.common.Response;
import com.neolynks.api.userapp.CartRequest;
import com.neolynks.curator.annotation.UserContextRequired;
import com.neolynks.curator.exception.CacheException;
import com.neolynks.curator.exception.InvalidCartIdException;
import com.neolynks.curator.manager.OrderHandler;
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

import lombok.extern.slf4j.Slf4j;

/**
 * Created by nitesh.garg on Dec 26, 2015
 */

@Slf4j
@Path("/curator/cart")
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    private final OrderHandler orderHandler;

    /**
     * @param cartEvaluator
     */
    public OrderResource(OrderHandler cartEvaluator) {
        super();
        this.orderHandler = cartEvaluator;
    }

    @Path("/init")
    @GET
    @UnitOfWork
    @UserContextRequired
    public Response<String> initialiseCart() {
        try {
            String cartId = this.orderHandler.initializeCart();
            Response<String> successResponse = Response.getSuccessResponse(cartId);
            return successResponse;
        }catch (CacheException e){
            log.error("Exception:", e);
            Response<String> failureResponse = Response.getFailureResponse(ErrorCode.CACHE_DOWN);
            return failureResponse;
        }catch (Exception e){
            log.error("Exception:", e);
            Response failureResponse = Response.getFailureResponse(ErrorCode.UNKNOWN_ERROR);
            return failureResponse;
        }
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
            this.orderHandler.setToCart(cartId, cartRequest.getItemCount());
            Response successResponse = Response.getSuccessResponse(null);
            return successResponse;
        }catch (InvalidCartIdException cim){
            Response failureResponse = Response.getFailureResponse(ErrorCode.MISSING_ORDER_ID);
            return failureResponse;
        }catch (Exception e){
            log.error("Exception:", e);
            Response failureResponse = Response.getFailureResponse(ErrorCode.UNKNOWN_ERROR);
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
            this.orderHandler.setCartStatus(cartId, statusId);
            Response successResponse = Response.getSuccessResponse(null);
            return successResponse;
        }catch (InvalidCartIdException cim){
            Response failureResponse = Response.getFailureResponse(ErrorCode.MISSING_ORDER_ID);
            return failureResponse;
        }catch (Exception e){
            log.error("Exception:", e);
            Response failureResponse = Response.getFailureResponse(ErrorCode.UNKNOWN_ERROR);
            return failureResponse;
        }
    }
}   