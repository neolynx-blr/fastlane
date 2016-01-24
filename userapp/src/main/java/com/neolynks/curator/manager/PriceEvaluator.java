package com.neolynks.curator.manager;

import com.google.common.cache.Cache;
import com.neolynks.api.common.inventory.InventoryInfo;
import com.neolynks.api.common.inventory.ItemInfo;
import com.neolynks.api.userapp.price.OrderPrice;
import com.neolynks.curator.dto.Order;
import lombok.AllArgsConstructor;

/**
 * Created by nishantgupta on 24/1/16.
 */
@AllArgsConstructor
public class PriceEvaluator {
    private final Cache<Long, Integer> vendorVersionCache;
    private final Cache<String, ItemInfo> vendorBarcodeInventoryCache;
    private final Cache<String, InventoryInfo> differentialInventoryCache;

    public OrderPrice getOrderPrice(Order order){
        //return OrderPrice post discount/tax calculation
        return new OrderPrice();
    }
}
