package com.example.helloworld.manager;

import java.util.ArrayList;
import java.util.List;

import com.example.helloworld.core.AllInventory;
import com.example.helloworld.core.Inventory;
import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.Product;
import com.example.helloworld.core.ProductCore;
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

		List<Inventory> itemList = new ArrayList<Inventory>();
		
		
		for(Inventory instance : inventoryList) {
			//itemList.add(new ProductCore(vendorId, instance.getName(), instance.getDescription(), instance.getTagline(), instance.getBarcode()));
		}
		
		response.setItemsAdded(itemList);
		
		return response;
	}
	
	public InventoryResponse getLatestInventory (Long vendorId) {
		
		// Validate vendorId, and dataVersionId
		
		// If dataVersionId is too old (30 days?), send back the full inventory
		
		// At load time, prepare all the differentials for various data versions, also, what define generation of a data version
		
		// For now, just send back everything

		InventoryResponse response = new InventoryResponse();
		
		//System.out.println("Data::" + this.allInventoryDAO.getLatestInventoryByVendor(1L).toString());
		
		List<AllInventory> inventoryList = this.allInventoryDAO.getLatestInventoryByVendor(vendorId);

		response.setVendorId(inventoryList.get(0).getVendorId());
		response.setDataVersionId(inventoryList.get(0).getVersionId());

		List<Inventory> itemList = new ArrayList<Inventory>();
		
		
		for(AllInventory instance : inventoryList) {
			Inventory invInstance = new Inventory();
			
			invInstance.setItemCode(instance.getItemCode());
			invInstance.setBarcode(instance.getBarcode());
			invInstance.setDescription(instance.getDescription());
			invInstance.setImageJSON(instance.getImageJSON());
			invInstance.setMrp(instance.getMrp());
			invInstance.setName(instance.getName());
			invInstance.setPrice(instance.getPrice());
			invInstance.setTagline(instance.getTagLine());
			invInstance.setVersionId(instance.getVersionId());
			invInstance.setVendorId(instance.getVendorId());
			invInstance.setDiscountType(instance.getDiscountType());
			invInstance.setDiscountValue(instance.getDiscountValue());
			System.out.println("Adding Data::"+invInstance.toString());
			itemList.add(invInstance);
		}
		
		response.setItemsAdded(itemList);
		
		return response;
	}
	
	public void setupInventoryVersions() {
		
	}

}
