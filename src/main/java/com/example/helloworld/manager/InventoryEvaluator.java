package com.example.helloworld.manager;

import com.example.helloworld.core.Inventory;
import com.example.helloworld.db.ProductDAO;

public class InventoryEvaluator {
	
    private final ProductDAO productDAO;

    public InventoryEvaluator(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }
	
	public Inventory getInventoryDifferential (Long vendorId, String dataVersionId) {
		
		Inventory returnInventory = new Inventory();
		
		// Validate vendorId, and dataVersionId
		
		// If dataVersionId is too old (30 days?), send back the full inventory
		
		// At load time, prepare all the differentials for various data versions, also, what define generation of a data version
		
		// For now, just send back everything
		returnInventory.setVendorId(vendorId);
		returnInventory.setDataVersionId(dataVersionId);
		
		returnInventory.setProductsAdded(productDAO.findAll());
		
		return returnInventory;
	}

}
