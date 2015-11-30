package com.neolynx.common.model.order;

import lombok.Data;

import com.neolynx.common.model.client.price.ItemPrice;

/**
 * Created by nitesh.garg on Nov 29, 2015
 *
 */

@Data
public class ItemProcessor {

	private String barcode;
	private String itemCode;
	
	private ItemPrice itemPrice;
	private Boolean isPricingChanged = false;
	
	private Integer countForDelivery = 0;
	private Integer countForInStorePickup = 1;
	private DeliveryMode itemDeliveryMode = DeliveryMode.IN_STORE_PICKUP;
	
	/**
	 * MRP is arbitrary number with no real implications other than sometimes being considered for discounts
	 * 
	 * fn(basePrice, tax%) = taxAmount
	 * 
	 * selling-price = basePrice + taxAmount - individualItemDiscountAmount
	 * netPrice = fn ( (count * price), cummulative-discount-if-applicable)
	 * 
	 */	
	private Double netPrice;
	private Double netTaxAmount;
	private Double netTaxableAmount;
	private Double netDiscountAmount;
	
	/**
	 * @param itemPrice the itemPrice to set
	 */
	public void setItemPrice(ItemPrice itemPrice) {
		if(this.getItemPrice().compareTo(itemPrice) != 0) {
			this.itemPrice = itemPrice;
			
			calculatePricing();
			this.setIsPricingChanged(Boolean.TRUE);
		}
	}

	public void setItemCount(int inStorePickUpCount, int deliveryCount) {

		boolean recalculatePrice = false;
		
		if(inStorePickUpCount == this.getCountForInStorePickup() && deliveryCount == this.getCountForDelivery()) {
			return;
		} else if((inStorePickUpCount + deliveryCount) != (this.getCountForDelivery() + this.getCountForInStorePickup())) {
			recalculatePrice = true;
		}
		
		if(inStorePickUpCount > 0 && deliveryCount > 0) {
			this.setItemDeliveryMode(DeliveryMode.PARTIAL_DELIVERY);
		} else if(inStorePickUpCount > 0) {
			this.setItemDeliveryMode(DeliveryMode.IN_STORE_PICKUP);
		} else {
			this.setItemDeliveryMode(DeliveryMode.DELIVERY);
		}
		
		this.setCountForDelivery(deliveryCount);
		this.setCountForInStorePickup(inStorePickUpCount);
		
		if(recalculatePrice) {
			calculatePricing();
			this.setIsPricingChanged(Boolean.TRUE);
		}
	}
	
	public ItemProcessor() {
	}
	
	public ItemProcessor(ItemRequest itemRequest) {
		this.setBarcode(itemRequest.getBarcode());
		this.setItemCode(itemRequest.getItemCode());
		this.setItemPrice(itemRequest.getItemPrice());
		this.setCountForDelivery(itemRequest.getCountForDelivery());
		this.setCountForInStorePickup(itemRequest.getCountForInStorePickup());
		
		this.setNetPrice(itemRequest.getNetPrice());
		this.setNetTaxAmount(itemRequest.getNetTaxAmount());
		this.setNetTaxableAmount(itemRequest.getNetTaxableAmount());
		this.setNetDiscountAmount(itemRequest.getNetDiscountAmount());
		
		if(this.getCountForInStorePickup() > 0 && this.getCountForDelivery() > 0) {
			this.setItemDeliveryMode(DeliveryMode.PARTIAL_DELIVERY);
		} else if(this.getCountForInStorePickup() > 0) {
			this.setItemDeliveryMode(DeliveryMode.IN_STORE_PICKUP);
		} else {
			this.setItemDeliveryMode(DeliveryMode.DELIVERY);
		}
	}
	
	public ItemRequest generateItemRequest() {
		
		ItemRequest request = new ItemRequest();

		request.setBarcode(this.getBarcode());
		request.setItemCode(this.getItemCode());
		request.setNetPrice(this.getNetPrice());
		request.setNetTaxAmount(this.getNetTaxAmount());
		request.setCountForDelivery(this.getCountForDelivery());
		request.setIsPricingChanged(this.getIsPricingChanged());
		request.setNetTaxableAmount(this.getNetTaxableAmount());
		request.setNetDiscountAmount(this.getNetDiscountAmount());
		request.setCountForInStorePickup(this.getCountForInStorePickup());
		
		return request;
	}
	
	public void calculatePricing() {
		this.setNetPrice(this.getItemPrice().getMrp() * (this.getCountForDelivery() + this.getCountForInStorePickup()));
	}

}
