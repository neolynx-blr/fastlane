package com.example.helloworld.manager;

import java.util.ArrayList;
import java.util.List;

import com.example.helloworld.core.Inventory;
import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.Product;
import com.example.helloworld.db.AllInventoryDAO;
import com.example.helloworld.db.InventoryDAO;

public class InventoryEvaluator {

    private	final InventoryDAO inventoryDAO;
    private	final AllInventoryDAO allInventoryDAO;

    public InventoryEvaluator(InventoryDAO inventoryDAO, AllInventoryDAO allInventoryDAO) {
		super();
		this.inventoryDAO = inventoryDAO;
		this.allInventoryDAO = allInventoryDAO;
	}

	public InventoryResponse getInventoryDifferential (Long vendorId, String dataVersionId) {
		
		// Validate vendorId, and dataVersionId
		
		// If dataVersionId is too old (30 days?), send back the full inventory
		
		// At load time, prepare all the differentials for various data versions, also, what define generation of a data version
		
		// For now, just send back everything

		InventoryResponse response = new InventoryResponse();
		
		System.out.println("Data::" + this.allInventoryDAO.findById(1L).toString());
		
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
	
	public void setupInventoryVersions() {
		
	}

}
