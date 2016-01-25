package com.neolynks.worker.resources;

import com.neolynks.api.common.ErrorCode;
import com.neolynks.api.common.Response;
import com.neolynks.worker.annotation.WorkerContextRequired;
import com.neolynks.worker.dto.WorkerSession;
import com.neolynks.worker.dto.WorkerTask;
import com.neolynks.worker.manager.WorkerSessionHandler;
import com.neolynks.worker.util.WorkerContextThreadLocal;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynks.common.model.client.InventoryInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@AllArgsConstructor
@Slf4j
public class WorkerResource {

    private final WorkerSessionHandler workerSessionHandler;
	
	@Path("/{vendorId}/current/all")
	@GET
	@UnitOfWork
	public InventoryInfo getLatestInventory(@PathParam(value = "vendorId") Long vendorId) {
		return null;
	}

    @Path("/{vendorId}/next")
    @GET
    @UnitOfWork
    @WorkerContextRequired
    public Response<WorkerTask> getNextAction() {
        try {
            String workerSessionId = WorkerContextThreadLocal.getWorkerVendorContextThreadLocal().get().getWorkerSessionId();
            WorkerTask workerTask =  workerSessionHandler.getWorkerTaskDetails(workerSessionId);
            Response<WorkerTask> successResponse = Response.getSuccessResponse(workerTask);
            return successResponse;
        }catch (Exception e){
            log.error("Exception:", e);
            Response failureResponse = Response.getFailureResponse(ErrorCode.UNKNOWN_ERROR);
            return failureResponse;
        }
    }


    @Path("/{vendorId}/status/{statusId}")
    @GET
    @UnitOfWork
    @WorkerContextRequired
    public Response<Void> updateWorkerStatus(@PathParam(value = "statusId") Integer statusId) {
        try {
            String workerSessionId = WorkerContextThreadLocal.getWorkerVendorContextThreadLocal().get().getWorkerSessionId();
            workerSessionHandler.setWorkerSessionStatus(workerSessionId, statusId);
            Response<Void> successResponse = Response.getSuccessResponse(null);
            return successResponse;
        }catch (Exception e){
            log.error("Exception:", e);
            Response failureResponse = Response.getFailureResponse(ErrorCode.UNKNOWN_ERROR);
            return failureResponse;
        }
    }

    @Path("/{vendorId}/int/{workerId}")
    @GET
    @UnitOfWork
    //TODO: Fix with proper authentication
    public Response<WorkerSession> initWorkerSession(@PathParam(value = "vendorId") Long vendorId,
                                              @PathParam(value = "workerId") String workerId) {
        try {
            WorkerSession workerSession =  workerSessionHandler.initWorkerSession(workerId, vendorId);
            Response<WorkerSession> successResponse = Response.getSuccessResponse(workerSession);
            return successResponse;
        }catch (Exception e){
            log.error("Exception:", e);
            Response failureResponse = Response.getFailureResponse(ErrorCode.UNKNOWN_ERROR);
            return failureResponse;
        }
    }

}
