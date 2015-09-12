package com.example.helloworld.manager;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.helloworld.core.InventoryMaster;
import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.ItemResponse;
import com.example.helloworld.db.InventoryMasterDAO;

/**
 * Created by nitesh.garg on 12-Sep-2015
 */
public class InventoryLoader {

	private final SessionFactory sessionFactory;
	private final InventoryMasterDAO invMasterDAO;
	static Logger LOGGER = LoggerFactory.getLogger(InventoryLoader.class);

	public InventoryLoader(SessionFactory sessionFactory, InventoryMasterDAO invMasterDAO) {
		super();
		this.sessionFactory = sessionFactory;
		this.invMasterDAO = invMasterDAO;
	}
	
	public void loadNewInventory(InventoryResponse inventoryResponse) {
	
		Long vendorId = inventoryResponse.getVendorId();
		Long dataVersionId = inventoryResponse.getNewDataVersionId();
		
		LOGGER.debug("Received request in loader for vendor-version [{}-{}]. RequestData:[{}]", vendorId, dataVersionId, inventoryResponse.getItemsUpdated().get(0).toString());
		
		for(ItemResponse instance : inventoryResponse.getItemsUpdated()) {
			
			InventoryMaster inventoryMaster = new InventoryMaster();
			inventoryMaster.setVendorId(vendorId);
			inventoryMaster.setVersionId(dataVersionId);
			
			inventoryMaster.setBarcode(instance.getBarcode());
			inventoryMaster.setCreatedOn(new java.sql.Date(System.currentTimeMillis()));
			inventoryMaster.setDescription(instance.getDescription());
			inventoryMaster.setDiscountType(instance.getDiscountType());
			inventoryMaster.setDiscountValue(instance.getDiscountValue());
			inventoryMaster.setImageJSON(instance.getImageJSON());
			inventoryMaster.setItemCode(instance.getItemCode());
			inventoryMaster.setMrp(instance.getMrp());
			inventoryMaster.setPrice(instance.getPrice());
			inventoryMaster.setTagLine(instance.getTagline());
			inventoryMaster.setName(instance.getName());
			
			this.invMasterDAO.create(inventoryMaster);
			
		}
		
	}
	
}
