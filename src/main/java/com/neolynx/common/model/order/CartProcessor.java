package com.neolynx.common.model.order;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * Created by nitesh.garg on Nov 29, 2015
 *
 * This class is not meant to be sent from/server but rather meant for local
 * processing both by the application as well as on server side if needed. On
 * client side, the application should do all cart handling operations using
 * this class as this allow calculating final pricing and all the core
 * operations like adding/removing items to cart as well as
 * incrementing/decrementing item counts. While communicating with the server,
 * simply extract a CartRequest object out of this class.
 */

@Data
public class CartProcessor {

	private Long vendorId;
	private Long deviceDataVersionId;

	private UserDetail userDetail = new UserDetail();
	private Map<String, ItemProcessor> barcodeItemRequestMap = new HashMap<String, ItemProcessor>();
	

	private Double taxAmount;
	private Double taxableAmount;
	private Double discountAmount;

	private Double netAmount;

	private int itemCount;
	private int totalCount;

	/**
	 * Following fields are meaningful only when order has once already been
	 * sent and created on server side. Next transaction will be more like
	 * updating the order, if anything.
	 */
	private String orderId;
	private Long lastKnownServerDataVersionId;

	private Boolean isItemListUpdated = Boolean.FALSE;
	private Boolean isUserDetailUpdated = Boolean.FALSE;

	/**
	 * Following fields are more of information flowing from server to client
	 * and something that may be needed at information to display on client side
	 * but not to be sent back to the server.
	 */
	private String orderStatus;
	private String orderBarcode;

	public ItemProcessor searchCartForBarcode(String barcode) {
		return this.getBarcodeItemRequestMap().get(barcode);
	}

	public void addItemToCart(ItemProcessor itemProcessor) {

		int newCount = itemProcessor.getCountForDelivery() + itemProcessor.getCountForInStorePickup();
		if (newCount == 0) {
			removeItemFromCart(itemProcessor.getBarcode());
			return;
		}

		boolean recalculatePrice = false;
		ItemProcessor existingItem = this.getBarcodeItemRequestMap().get(itemProcessor.getBarcode());

		if (existingItem == null) {
			recalculatePrice = true;
			this.setIsItemListUpdated(Boolean.TRUE);
			this.setItemCount(this.getItemCount() + 1);
			this.setTotalCount(this.getTotalCount() + newCount);
		} else {
			int existingCount = existingItem.getCountForDelivery() + existingItem.getCountForInStorePickup();
			if (newCount != existingCount) {
				recalculatePrice = true;
				this.setIsItemListUpdated(Boolean.TRUE);
				this.setTotalCount(this.getTotalCount() + newCount - existingCount);
			}
		}

		this.getBarcodeItemRequestMap().put(itemProcessor.getBarcode(), itemProcessor);

		if (recalculatePrice) {
			calculatePricing();
		}
	}
	
	public void removeItemFromCart(String barcode) {

		ItemProcessor existingItem = this.getBarcodeItemRequestMap().get(barcode);
		int existingCount = existingItem.getCountForDelivery() + existingItem.getCountForInStorePickup();

		this.setIsItemListUpdated(Boolean.TRUE);
		this.setItemCount(this.getItemCount() - 1);
		this.getBarcodeItemRequestMap().remove(barcode);
		this.setTotalCount(this.getTotalCount() - existingCount);

		calculatePricing();
	}
	
	public void setUserDetailsForCart(String userId, String deviceIdMap, Integer addressId) {
		this.getUserDetail().setUserId(userId);
		this.getUserDetail().setAddressId(addressId);
		this.getUserDetail().setDeviceIdMap(deviceIdMap);

		this.setIsUserDetailUpdated(Boolean.TRUE);
	}

	public CartRequest generateRequest() {
		CartRequest request = new CartRequest();

		request.setVendorId(this.getVendorId());
		request.setItemCount(this.getItemCount());
		request.setTotalCount(this.getTotalCount());
		request.setUserDetail(this.getUserDetail());
		request.setDeviceDataVersionId(this.getDeviceDataVersionId());

		for (ItemProcessor instance : this.getBarcodeItemRequestMap().values()) {
			request.getItemList().add(instance.generateItemRequest());
		}
		request.setNetAmount(this.getNetAmount());
		request.setTaxAmount(this.getTaxAmount());
		request.setTaxableAmount(this.getTaxableAmount());
		request.setDiscountAmount(this.getDiscountAmount());
		
		if (this.getOrderId() != null) {
			CartUpdated updateCart = new CartUpdated();
			updateCart.setOrderId(this.getOrderId());
			updateCart.setIsItemListUpdated(this.getIsItemListUpdated());
			updateCart.setIsUserDetailUpdated(this.getIsUserDetailUpdated());
			updateCart.setLastKnownServerDataVersionId(this.getLastKnownServerDataVersionId());
			request.setUpdateCart(updateCart);
		}

		return request;
	}

	public void mergeResponseToCart(Response cartResponse) {

		this.setOrderId(cartResponse.getOrderId());
		this.setOrderStatus(cartResponse.getOrderStatus());
		this.setOrderBarcode(cartResponse.getOrderBarcode());
		this.setLastKnownServerDataVersionId(cartResponse.getServerDataVersionId());

		if (cartResponse.getUpdateResponseInfo() != null) {
			this.setTaxAmount(cartResponse.getUpdateResponseInfo().getTaxAmount());
			this.setNetAmount(cartResponse.getUpdateResponseInfo().getNetAmount());
			this.setTaxableAmount(cartResponse.getUpdateResponseInfo().getTaxableAmount());
			this.setDiscountAmount(cartResponse.getUpdateResponseInfo().getDiscountAmount());
		}

		for (ItemRequest instance : cartResponse.getUpdateResponseInfo().getOnlyUpdatedItemList()) {
			this.getBarcodeItemRequestMap().put(instance.getBarcode(), new ItemProcessor(instance));
		}

	}



	public void calculatePricing() {
		Double netPrice = 0.0D;
		for (ItemProcessor instance : this.getBarcodeItemRequestMap().values()) {
			netPrice += instance.getNetPrice();
		}
		this.setNetAmount(netPrice);
	}

}
