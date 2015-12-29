package com.neolynks.curator.manager;

import java.util.HashMap;
import java.util.Set;

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynks.common.model.cart.CartResponse;
import com.neolynks.common.model.cart.CartStatus;
import com.neolynks.common.model.order.CartPreview;
import com.neolynks.common.model.order.ItemRequest;
import com.neolynks.curator.meta.CartLogistics;
import com.neolynks.curator.meta.DataEvaluator;
import com.neolynks.curator.meta.UserInfo;
import com.neolynks.curator.meta.VendorInfo;
import com.neolynks.curator.model.Cart;
import com.neolynks.curator.util.RandomString;

/**
 * Created by nitesh.garg on Dec 26, 2015
 *
 */

@Data
public class CartHandler {

	private final LoadingCache<String, Cart> cartCache;
	private final LoadingCache<Long, Long> vendorVersionCache;
	
	static Logger LOGGER = LoggerFactory.getLogger(CartHandler.class);

	public CartResponse initializeCart(Long vendorId, Long userId) {

		CartResponse response = new CartResponse();

		UserInfo userInfo = DataEvaluator.getUserDetails(userId);
		VendorInfo vendorInfo = DataEvaluator.getVendorDetails(vendorId);

		Cart cart = new Cart();

		cart.getBase().setUserId(userInfo.getUserId());
		cart.getBase().setVendorId(vendorInfo.getVendorId());
		cart.setLatestVendorDataVersionId(vendorInfo.getLatestDataVersionId());

		boolean isCartIdUnique = false;
		String cartId = vendorInfo.getVendorAbbr() + "-" + RandomString.nextCartId();
		while (!isCartIdUnique) {
			// TODO Check for uniqueness
			isCartIdUnique = true;
		}
		cart.getBase().setId(cartId);

		synchronized (cartId) {
			this.cartCache.put(cartId, cart);
			CartLogistics.getInstance().getUpdatedCartIds().add(cartId);
		}

		LOGGER.debug("Cart [{}], for user [{}] and vendor [{}], initialized and loaded in cache", cartId,
				userInfo.getUserId(), vendorInfo.getVendorId());

		response.setCartBase(cart.getBase());

		return response;
	}
	
	public CartResponse setCartContent(String cartId, CartPreview cartPreview) {
		
		// TODO: data validator, properly populate the item-request object
		
		CartResponse response = new CartResponse();
		Cart cart = this.cartCache.getIfPresent(cartId);
		
		if(cart == null) {
			response.setIsError(Boolean.TRUE);
			return response;
		}
		
		cart.setAdminSyncedBarcodeCount(new HashMap<Long, Integer>());
		cart.setCartSyncedWithAdmin(Boolean.FALSE);
		cart.setDiscountAmount(0.0D);
		cart.setItemList(new HashMap<Long, ItemRequest>());
		cart.setNetAmount(0.0D);
		cart.setTaxableAmount(0.0D);
		cart.setTaxAmount(0.0D);

		Integer totalItemCount = 0;
		Integer uniqueItemCount = 0;
		
		Set<Long> itemBarcodeList = cartPreview.getItemBarcodeCountMap().keySet();

		for(Long barcode : itemBarcodeList) {

			uniqueItemCount++;
			totalItemCount += cartPreview.getItemBarcodeCountMap().get(barcode);
			
			ItemRequest itemRequest = new ItemRequest();
			itemRequest.setBarcode(barcode);
			itemRequest.setCountForInStorePickup(cartPreview.getItemBarcodeCountMap().get(barcode));
			
			cart.getItemList().put(barcode, itemRequest);
			
		}
		
		cart.getBase().setTotalItemCount(totalItemCount);
		cart.getBase().setUniqueItemCount(uniqueItemCount);

		cart.getBase().setDeliveryMode(cartPreview.getDeliveryMode());
		
		synchronized (cartId) {
			cart.setCartSyncedWithAdmin(Boolean.FALSE);
			this.cartCache.put(cartId, cart);
			
			CartLogistics.getInstance().getUpdatedCartIds().add(cartId);
			CartLogistics.getInstance().getSyncedCartIds().remove(cartId);
		}
		
		return response;
		
	}

	public CartResponse initializeNSetCart(CartPreview cartPreview) {
		
		// TODO error handling
		
		CartResponse response = initializeCart(cartPreview.getVendorId(), cartPreview.getUserId());
		
		String cartId = response.getCartBase().getId();
		Cart cart = this.cartCache.getIfPresent(cartId);

		if(cart == null) {
			response.setIsError(Boolean.TRUE);
			return response;
		}

		response = setCartContent(cartId, cartPreview);
		
		return response;
	}

	public CartResponse setToCart(String cartId, Long barcode, Integer count) {

		// TODO: data validator, populate item-request properly
		CartResponse response = new CartResponse();
		
		Cart cart = this.cartCache.getIfPresent(cartId);
		
		if(cart == null || !(cart.getBase().getStatus() == CartStatus.OPEN)) {
			response.setIsError(Boolean.TRUE);
			return response;
		}
		
		ItemRequest item = cart.getItemList().get(barcode);

		if (item == null) {

			ItemRequest newItem = new ItemRequest();

			newItem.setBarcode(barcode);
			newItem.setCountForInStorePickup(count);

			cart.getItemList().put(barcode, newItem);
			cart.setCartSyncedWithAdmin(Boolean.FALSE);
			
			cart.getBase().setUniqueItemCount(cart.getBase().getUniqueItemCount()+1);
			cart.getBase().setTotalItemCount(cart.getBase().getTotalItemCount()+count);
			
			LOGGER.debug("Added barcode [{}] ([{}] times) to user [{}]'s cart [{}]", barcode, count, cart.getBase().getUserId(), cart.getBase().getId());

		} else {

			Integer existingItemCount = item.getCountForDelivery() + item.getCountForInStorePickup();
			
			if (count == 0) {
				cart.getItemList().remove(barcode);
				cart.getBase().setUniqueItemCount(cart.getBase().getUniqueItemCount()-1);
				cart.getBase().setTotalItemCount(cart.getBase().getTotalItemCount()-existingItemCount);
				LOGGER.debug("Removed barcode [{}] from user [{}]'s cart [{}]", barcode, cart.getBase().getUserId(), cart.getBase().getId());
			} else {
				item.setCountForInStorePickup(count);
				cart.getItemList().put(barcode, item);
				cart.getBase().setTotalItemCount(cart.getBase().getTotalItemCount()-existingItemCount+count);
				LOGGER.debug("Updated barcode [{}] from user [{}]'s cart [{}] to count [{}] from [{]]", barcode, cart.getBase().getUserId(), cart.getBase().getId(), count, existingItemCount);
			}
			
		}
		
		synchronized (cartId) {
			cart.setCartSyncedWithAdmin(Boolean.FALSE);
			this.cartCache.put(cartId, cart);
			
			CartLogistics.getInstance().getUpdatedCartIds().add(cartId);
			CartLogistics.getInstance().getSyncedCartIds().remove(cartId);
		}
		
		response.setCartBase(cart.getBase());

		return response;
	}
	
	public CartResponse setCartStatus(String cartId, Integer statusId) {

		// TODO: data validator, add proper flow of state changes among status
		CartResponse response = new CartResponse();
		
		Cart cart = this.cartCache.getIfPresent(cartId);
		
		if(cart == null) {
			response.setIsError(Boolean.TRUE);
			return response;
		}
		
		synchronized (cartId) {
		
			switch(statusId) {
			
				case 1:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getBase().getStatus().name(), CartStatus.OPEN.name());
					cart.getBase().setStatus(CartStatus.OPEN);
					break;
	
				case 2:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getBase().getStatus().name(), CartStatus.IN_PREPARATION.name());
					cart.getBase().setStatus(CartStatus.IN_PREPARATION);
					CartLogistics.getInstance().getClosedCartIds().add(cartId);
					break;
	
				case 3:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getBase().getStatus().name(), CartStatus.PENDING_USER_REVIEW.name());
					cart.getBase().setStatus(CartStatus.PENDING_USER_REVIEW);
					break;
	
				case 4:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getBase().getStatus().name(), CartStatus.PENDING_PAYMENT.name());
					cart.getBase().setStatus(CartStatus.PENDING_PAYMENT);
					break;
	
				case 5:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getBase().getStatus().name(), CartStatus.PENDING_DELIVERY.name());
					cart.getBase().setStatus(CartStatus.PENDING_DELIVERY);
					break;
	
				case 6:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getBase().getStatus().name(), CartStatus.PENDING_PARTIAL_DELIVERY.name());
					cart.getBase().setStatus(CartStatus.PENDING_PARTIAL_DELIVERY);
					break;
	
				case 7:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getBase().getStatus().name(), CartStatus.COMPLETE.name());
					cart.getBase().setStatus(CartStatus.COMPLETE);
					break;
	
				default:
					LOGGER.debug("Unable to understand status [{}] while updating status for cary [{}] ", statusId, cartId);
			
			}
	
			cart.setCartSyncedWithAdmin(Boolean.FALSE);
			this.cartCache.put(cartId, cart);
			
			CartLogistics.getInstance().getUpdatedCartIds().add(cartId);
			CartLogistics.getInstance().getSyncedCartIds().remove(cartId);

		}

		response.setCartBase(cart.getBase());

		return response;
	}

	public CartHandler(LoadingCache<String, Cart> cartCache, LoadingCache<Long, Long> vendorVersionCache) {
		super();
		
		this.cartCache = cartCache;
		this.vendorVersionCache = vendorVersionCache;
	}


}
