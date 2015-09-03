package com.example.helloworld.manager;

import java.util.ArrayList;
import java.util.List;

import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.ItemResponse;
import com.example.helloworld.db.ItemResponseDAO;

public class InventoryEvaluator {

    private final ItemResponseDAO itemResponseDAO;

    public InventoryEvaluator(ItemResponseDAO itemResponseDAO) {
		super();
		this.itemResponseDAO = itemResponseDAO;
	}

	public InventoryResponse getInventoryDifferential (Long vendorId, String dataVersionId) {
		InventoryResponse response = new InventoryResponse();
		return response;
	}
	
	public InventoryResponse getLatestInventory (Long vendorId) {

		Long dataVersionId = -1L;
		InventoryResponse response = new InventoryResponse();
		
		List<ItemResponse> latestVendorItems = this.itemResponseDAO.getLatestInventory(vendorId); 

		response.setVendorId(vendorId);
		
		List<ItemResponse> itemList = new ArrayList<ItemResponse>();

		for(ItemResponse instance : latestVendorItems) {
			if(instance.getVersionId() > dataVersionId) {
				dataVersionId = instance.getVersionId();
			}
			itemList.add(instance);
		}

		response.setDataVersionId(dataVersionId);
		response.setItemsAdded(itemList);
		
		return response;
	}
	
}
