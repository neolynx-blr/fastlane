package com.example.helloworld.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.ItemResponse;
import com.example.helloworld.db.ItemResponseDAO;
import com.google.common.cache.LoadingCache;

public class InventoryEvaluator {

	private final ItemResponseDAO itemResponseDAO;
	private final LoadingCache<String, InventoryResponse> differentialInventoryCache;

	public InventoryEvaluator(ItemResponseDAO itemResponseDAO,
			LoadingCache<String, InventoryResponse> differentialInventoryCache) {
		super();
		this.itemResponseDAO = itemResponseDAO;
		this.differentialInventoryCache = differentialInventoryCache;
	}

	public InventoryResponse getInventoryDifferential(Long vendorId, String dataVersionId) {
		InventoryResponse inventoryResponse = null;
		try {
			inventoryResponse = this.differentialInventoryCache.get(vendorId + "-" + dataVersionId);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (inventoryResponse == null) {
			inventoryResponse = getLatestInventory(vendorId);
		}
		return inventoryResponse;
	}

	public InventoryResponse getLatestInventory(Long vendorId) {

		Long dataVersionId = -1L;
		InventoryResponse response = new InventoryResponse();

		List<ItemResponse> latestVendorItems = this.itemResponseDAO.getLatestInventory(vendorId);

		response.setVendorId(vendorId);

		List<ItemResponse> itemList = new ArrayList<ItemResponse>();

		for (ItemResponse instance : latestVendorItems) {
			if (instance.getVersionId() > dataVersionId) {
				dataVersionId = instance.getVersionId();
			}
			itemList.add(instance);
		}

		response.setNewDataVersionId(dataVersionId);
		response.setItemsAdded(itemList);

		return response;
	}

}
