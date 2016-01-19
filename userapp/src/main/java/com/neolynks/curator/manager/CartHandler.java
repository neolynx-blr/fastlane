package com.neolynks.curator.manager;

import java.util.Map;

import com.google.common.cache.Cache;
import com.neolynks.CartSignalExchange;
import com.neolynks.api.common.CartStatus;
import com.neolynks.api.common.UserVendorContext;
import com.neolynks.curator.exception.InvalidCartIdException;
import com.neolynks.curator.util.UserContextThreadLocal;
import com.neolynks.dao.OrderDetailDAO;
import com.neolynks.dto.CartDelta;
import com.neolynks.dto.CartOperation;
import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynks.curator.meta.DataEvaluator;
import com.neolynks.curator.meta.VendorInfo;
import com.neolynks.curator.dto.Cart;
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

	public String initializeCart() {
        UserVendorContext userVendorContext = UserContextThreadLocal.getUserVendorContextLocale().get();
		VendorInfo vendorInfo = DataEvaluator.getVendorDetails(userVendorContext.getVendorInventorySnap().getVendorId());

		Cart cart = new Cart();
	    cart.setUserVendorContext(userVendorContext);
		String cartId = RandomString.nextCartId();
        cart.setCartId(cartId);

		this.cartCache.put(cartId, cart);

		LOGGER.debug("Cart [{}], for user [{}] and vendor [{}], initialized and loaded in cache", cartId,
                userVendorContext.getUserId(), vendorInfo.getVendorId());
		return cartId;
	}
	
	public void setToCart(String cartId, Map<String, Integer> itemCount) throws InvalidCartIdException {
		Cart cart = this.cartCache.getIfPresent(cartId);
		
		if(cart == null || !CartStatus.OPEN.equals(cart.getStatus())) {
            throw InvalidCartIdException.getDefaut();
		}

        for (String barcode : itemCount.keySet()) {
            Integer existingItemCount = cart.getItemCount().get(barcode);
            Integer newItemCount = itemCount.get(barcode);
            if (newItemCount == 0) {
                cart.getItemCount().remove(barcode);
                CartSignalExchange.getInstance().addCartDelta(new CartDelta(cartId, barcode, 0, CartDelta.Operation.REMOVED));
                LOGGER.debug("Removed barcode [{}] from user [{}]'s cart [{}]", barcode, cartId);
            } else if (existingItemCount == null) {
                cart.getItemCount().put(barcode, newItemCount);
                LOGGER.debug("Updated barcode [{}] from user [{}]'s cart [{}] to count [{}] from [{]]", barcode, newItemCount, existingItemCount);
                CartSignalExchange.getInstance().addCartDelta(new CartDelta(cartId, barcode, newItemCount, CartDelta.Operation.ADDED));
            } else {
                cart.getItemCount().put(barcode, newItemCount + existingItemCount);
                CartSignalExchange.getInstance().addCartDelta(new CartDelta(cartId, barcode, newItemCount, CartDelta.Operation.ADDED));
                LOGGER.debug("Updated barcode [{}] from user [{}]'s cart [{}] to count [{}] from [{]]", barcode, newItemCount, existingItemCount);
            }
        }

	}

    public void setCartStatus(String cartId, Integer statusId) throws InvalidCartIdException {
        Cart cart = this.cartCache.getIfPresent(cartId);
        if (cart == null) {
            throw InvalidCartIdException.getDefaut();
        }

        switch (statusId) {
            case 1:
                LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.OPEN.name());
                cart.setStatus(CartStatus.OPEN);
                break;
            case 2:
                LOGGER.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.IN_PREPARATION.name());
                cart.setStatus(CartStatus.IN_PREPARATION);
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
                LOGGER.debug("Unable to understand status [{}] while updating status for cart [{}] ", statusId, cartId);

        }
        CartSignalExchange.getInstance().addCartDelta(new CartOperation(cartId, cart.getUserVendorContext().getVendorInventorySnap().getVendorId(), cart.getStatus().getValue()));
    }

}
