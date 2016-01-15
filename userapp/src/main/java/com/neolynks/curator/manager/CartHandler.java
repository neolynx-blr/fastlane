package com.neolynks.curator.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.cache.Cache;
import com.neolynks.api.common.CartStatus;
import com.neolynks.api.common.ErrorCode;
import com.neolynks.api.common.Response;
import com.neolynks.api.common.UserVendorContext;
import com.neolynks.curator.util.UserContextThreadLocal;
import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynks.curator.db.OrderDetailDAO;
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

	private final OrderDetailDAO orderDetailDAO;
	private final Cache<String, Cart> cartCache;
	private final LoadingCache<Long, Long> vendorVersionCache;
	
	static Logger LOGGER = LoggerFactory.getLogger(CartHandler.class);
	
	public CartHandler(Cache<String, Cart> cartCache, LoadingCache<Long, Long> vendorVersionCache, OrderDetailDAO orderDetailDAO) {
		super();
		
		this.cartCache = cartCache;
		this.orderDetailDAO = orderDetailDAO;
		this.vendorVersionCache = vendorVersionCache;
		
	}

	public Response<String>  initializeCart() {
        Response<String> cartInitResp = new Response<>();
        UserVendorContext userVendorContext = UserContextThreadLocal.getUserVendorContextLocale().get();
		UserInfo userInfo = DataEvaluator.getUserDetails(userVendorContext.getUserId());
		VendorInfo vendorInfo = DataEvaluator.getVendorDetails(userVendorContext.getVendorInventorySnap().getVendorId());

		Cart cart = new Cart();
	    cart.setUserVendorContext(userVendorContext);
		String cartId = RandomString.nextCartId();
        cart.setCartId(cartId);

		this.cartCache.put(cartId, cart);
		CartLogistics.getInstance().getUpdatedCartIds().add(cartId);

		LOGGER.debug("Cart [{}], for user [{}] and vendor [{}], initialized and loaded in cache", cartId,
				userInfo.getUserId(), vendorInfo.getVendorId());

        cartInitResp.setData(cartId);
        cartInitResp.setIsError(false);
		return cartInitResp;
	}
	
	public CartResponse setCartContent(Long cartId, CartPreview cartPreview) {
		
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

	public Response<Void> setToCart(String cartId, Map<String, Integer> itemCount) {
        Response<Void> response = new Response<>();
		Cart cart = this.cartCache.getIfPresent(cartId);
		
		if(cart == null || !CartStatus.OPEN.equals(cart.getStatus())) {
			response.setIsError(Boolean.TRUE);
            response.setErrorDetail(new ArrayList<ErrorCode>() {{
                add(ErrorCode.MISSING_ORDER_ID);

            }});
			return response;
		}

        for(String barcode: itemCount.keySet()){
            Integer count = cart.getItemCount().get(barcode);

            if (count == null || count==0) {
                cart.getItemCount().put(barcode, itemCount.get(barcode));
                cart.setCartSyncedWithWorker(Boolean.FALSE);
                LOGGER.debug("Added barcode [{}] ([{}] times) to cart [{}]", barcode, count, cartId);
            } else {
                Integer existingItemCount = cart.getItemCount().get(barcode);
                if (count == 0) {
                    cart.getItemCount().remove(barcode);
                    LOGGER.debug("Removed barcode [{}] from user [{}]'s cart [{}]", barcode, cartId);
                } else {
                    cart.getItemCount().put(barcode, itemCount.get(barcode) + count);
                    cart.setCartSyncedWithWorker(Boolean.FALSE);
                    LOGGER.debug("Updated barcode [{}] from user [{}]'s cart [{}] to count [{}] from [{]]", barcode, count, existingItemCount);
                }

            }
        }

        cart.setCartSyncedWithWorker(Boolean.FALSE);

        CartLogistics.getInstance().getUpdatedCartIds().add(cartId);
        CartLogistics.getInstance().getSyncedCartIds().remove(cartId);

        response.setIsError(false);
        return response;
	}
	
	public Response<Void> setCartStatus(String cartId, Integer statusId) {

		// TODO: data validator, add proper flow of state changes among status
        Response<Void> response = new Response<Void>();
		
		Cart cart = this.cartCache.getIfPresent(cartId);
		
		if(cart == null) {
			response.setIsError(Boolean.TRUE);
			return response;
		}
		
		synchronized (cartId) {
		
			switch(statusId) {
			
				case 1:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getBase().getStatus().name(), CartStatus.OPEN.name());
					cart.setStatus(CartStatus.OPEN);
					break;
	
				case 2:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getBase().getStatus().name(), CartStatus.IN_PREPARATION.name());
					cart.setStatus(CartStatus.IN_PREPARATION);
					CartLogistics.getInstance().getClosedCartIds().add(cartId);
					break;
	
				case 3:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.PENDING_USER_REVIEW.name());
					cart.setStatus(CartStatus.PENDING_USER_REVIEW);
					break;
	
				case 4:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.PENDING_PAYMENT.name());
					cart.setStatus(CartStatus.PENDING_PAYMENT);
					break;
	
				case 5:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.PENDING_DELIVERY.name());
					cart.setStatus(CartStatus.PENDING_DELIVERY);
					break;
	
				case 6:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.PENDING_PARTIAL_DELIVERY.name());
					cart.setStatus(CartStatus.PENDING_PARTIAL_DELIVERY);
					break;
	
				case 7:
					LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.COMPLETE.name());
					cart.setStatus(CartStatus.COMPLETE);
					break;
	
				default:
					LOGGER.debug("Unable to understand status [{}] while updating status for cary [{}] ", statusId, cartId);
			
			}
	
			cart.setCartSyncedWithWorker(Boolean.FALSE);
			this.cartCache.put(cartId, cart);
			
			CartLogistics.getInstance().getUpdatedCartIds().add(cartId);
			CartLogistics.getInstance().getSyncedCartIds().remove(cartId);

		}
		response.setIsError(false);
		return response;
	}

}
