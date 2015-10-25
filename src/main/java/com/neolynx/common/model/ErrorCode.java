/**
 * 
 */
package com.neolynx.common.model;

/**
 * Created by nitesh.garg on Oct 25, 2015
 *
 */
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
	
	FAILED_CLEANING_VENDOR_PRODUCT_MASTER_RECORDS("DBE001", "Unable to remove vendor records from product-master table."),
	FAILED_CLEANING_VENDOR_ITEM_MASTER_RECORDS("DBE002", "Unable to remove vendor records from item-master table."),
	ORDER_ID_DOESNT_EXIST_IN_DB("DB003", "The order-id checked ofr doesn't exist in the database."),
	
	INVALID_VENDOR_ID ("DVE001", "Invalid or Missing Vendor Id"),
	MISSING_VENDOR_INVENTORY_DATA ("DVE002", "Missing data while generating inventory master data file for the vendor."),
	UNKNOWN_INVENTORY_VERSION_ON_DEVICE ("DVE003", "Unknown inventory data version on device side."),
	UNKNOWN_INVENTORY_VERSION_ON_SERVER ("DVE004", "Unknown inventory data version on server side."),
	MISSING_ITEMS_IN_CART ("DVE005", "No items are found in the cart while placing the order."),
	MISSING_ITEMS_TO_BE_UPDATED_IN_CART ("DVE006", "No items are found in cart to be added/updated while updating the order"),
	MISSING_USER_DETAIL_FOR_DELIVERY ("DVE007", "No user associated with order marked for delivery.");

	private final String code;
	private final String description;

	private ErrorCode(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getCode() {
		return code;
	}

	@Override
	public String toString() {
		return code + ": " + description;
	}

}
