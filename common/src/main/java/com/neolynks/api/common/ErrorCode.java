/**
 * 
 */
package com.neolynks.api.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by nitesh.garg on Oct 25, 2015
 *
 */
@AllArgsConstructor
public enum ErrorCode {

	/**
	 * 1. DVE: Data validation errors
	 * 2. ILE: Inventory load errors
	 * 3. CPE: Cache processing errors
	 * 4. DBE: Database Processing Errors
	 */
	
	MISSING_INVENTORY_FOR_LOAD ("ILE001", "No inventory records are available for vendor to replace existing inventory."),
	
	FAILED_CLEANING_VENDOR_DIFFERENTIAL_CACHE("CPE001", "Unable to remove vendor data from version differential cache."),
	FAILED_CLEANING_VENDOR_VERSION_CACHE("CPE002", "Unable to remove vendor data from all-version cache."),
    CACHE_DOWN("CPE003", "Cache is down"),
	
	FAILED_CLEANING_VENDOR_PRODUCT_MASTER_RECORDS("DBE001", "Unable to remove vendor records from product-master table."),
	FAILED_CLEANING_VENDOR_ITEM_MASTER_RECORDS("DBE002", "Unable to remove vendor records from item-master table."),
	ORDER_ID_DOESNT_EXIST_IN_DB("DB003", "The order-id checked ofr doesn't exist in the database."),
	
	INVALID_VENDOR_ID ("DVE001", "Invalid or Missing Vendor Id"),
	MISSING_VENDOR_INVENTORY_DATA ("DVE002", "Missing data while generating inventory master data file for the vendor."),
	UNKNOWN_INVENTORY_VERSION_ON_DEVICE ("DVE003", "Unknown inventory data version on device side."),
	UNKNOWN_INVENTORY_VERSION_ON_SERVER ("DVE004", "Unknown inventory data version on server side."),
	MISSING_ITEMS_IN_CART ("DVE005", "No items are found in the cart while placing the order."),
	MISSING_ITEMS_TO_BE_UPDATED_IN_CART ("DVE006", "No items are found in cart to be added/updated while updating the order"),
	MISSING_USER_DETAILS_FOR_DELIVERY ("DVE007", "User details (identifier/address) missing from the request set for delivery."),
	EXPIRED_DATA_VERSION ("DVE008", "The data version found on device is now expired, please refresh the inventory ASAP."),
	INVALID_OR_MISSING_ITEM_CODE ("DVE009", "Item code is missing"),
	INVALID_OR_MISSING_BARCODE ("DVE010", "Barcode is missing"),
	INVALID_COUNT_OF_ITEM_IN_ORDER ("DVE011", "Invalid number of items found in Cart object against count of items found"),
	INVALID_TOTAL_COUNT_OF_ITEM_IN_ORDER ("DVE012", "Invalid total number of items found in Cart object against count of items found"),
	INCONSISTENT_DELIVERY_MODE_AGAINT_ORDER_ITEMS ("DVE013", "Delivery mode is found inconsistent among the cart against at least one item"),
	MISSING_UPDATE_CART_DETAILS ("DVE014", "Cart received for update order operation is missing the update structure within the cart"),
	MISSING_UPDATE_OPERATION_REASON ("DVE015", "Update order call is missing whether item list or user details are being updated"),
	MISSING_OR_INVALID_LAST_SEEN_SERVER_DATA_VERSION_ID ("DVE016", "Update order call is missing last seen server data version id"),
	MISSING_ORDER_ID ("DVE017", "Update order call is missing the order-id received in the create call"),
    INVALID_STATUS_ID("DVE018", "Invalid status id"),
    INVALID_STATUS_TRANSITION("DVE019", "Invalid status id transition"),

    UNKNOWN_ERROR ("UNK001", "Some unknown error");

    @Getter
	private final String code;

    @Getter
	private final String description;
}
