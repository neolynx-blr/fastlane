package com.neolynks.vendor;

import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynks.curator.core.User;
import com.neolynks.vendor.manager.InventoryService;
import com.neolynks.common.model.BaseResponse;

/**
 * Purpose of this class is to invoke methods on vendor side like generating
 * full inventory file etc.
 * 
 * Created by nitesh.garg on Oct 2, 2015
 */

@Path("/vendor")
@Produces(MediaType.APPLICATION_JSON)
public class ClientResource {

	private final InventoryService inventoryService;

	public ClientResource(InventoryService inventoryService) {
		super();
		this.inventoryService = inventoryService;
	}

	@Path("/generate-inventory-master/")
	@GET
	@RolesAllowed("Administrator")
	@UnitOfWork
	public BaseResponse generateInventoryMaster(@Auth User user) {
		return this.inventoryService.generateInventoryMasterCSV();
	}

}
