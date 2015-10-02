package com.neolynx.vendor;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.neolynx.common.model.BaseResponse;
import com.neolynx.vendor.manager.InventoryService;

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
	@UnitOfWork
	public BaseResponse generateInventoryMaster() {
		return this.inventoryService.generateInventoryMasterCSV();
	}

}
