package com.example.helloworld.manager;

import java.util.ArrayList;
import java.util.List;

import com.example.helloworld.core.Inventory;
import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.Product;
import com.example.helloworld.db.InventoryDAO;

public class InventoryEvaluator {

    private	final InventoryDAO inventoryDAO;

    public InventoryEvaluator(InventoryDAO inventoryDAO) {
		super();
		this.inventoryDAO = inventoryDAO;
	}

	public InventoryResponse getInventoryDifferential (Long vendorId, String dataVersionId) {
		
		// Validate vendorId, and dataVersionId
		
		// If dataVersionId is too old (30 days?), send back the full inventory
		
		// At load time, prepare all the differentials for various data versions, also, what define generation of a data version
		
		// For now, just send back everything

		InventoryResponse response = new InventoryResponse();
		
		List<Inventory> inventoryList = inventoryDAO.getLatestInventory(vendorId);

		response.setVendorId(inventoryList.get(0).getVendorId());
		response.setDataVersionId(inventoryList.get(0).getVersionId());

		List<Product> itemList = new ArrayList<Product>();
		
		
		for(Inventory instance : inventoryList) {
			itemList.add(new Product(instance.getProductId(), instance.getName(), instance.getBarcode(), instance.getTagline(), instance.getDescription(), instance.getImageJSON()));
		}
		
		response.setItemsAdded(itemList);
		
		return response;
	}

}
