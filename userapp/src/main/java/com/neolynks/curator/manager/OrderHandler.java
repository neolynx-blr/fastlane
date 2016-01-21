package com.neolynks.curator.manager;

import java.sql.Timestamp;
import java.util.Map;

import com.google.common.cache.Cache;
import com.neolynks.CartSignalExchange;
import com.neolynks.api.common.CartStatus;
import com.neolynks.api.common.UserVendorContext;
import com.neolynks.curator.exception.InvalidCartIdException;
import com.neolynks.curator.util.UserContextThreadLocal;
import com.neolynks.dao.OrderDetailDAO;
import com.neolynks.dao.UnderProcessingOrderDAO;
import com.neolynks.dto.CartDelta;
import com.neolynks.dto.CartOperation;
import com.neolynks.model.UnderProcessingOrder;
import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
@AllArgsConstructor
public class OrderHandler {

    private final UnderProcessingOrderDAO underProcessingOrderDAO;
	private final OrderDetailDAO orderDetailDAO;
	private final Cache<String, Cart> cartCache;
	private final LoadingCache<Long, Long> vendorVersionCache;

	public String initializeCart() {
        UserVendorContext userVendorContext = UserContextThreadLocal.getUserVendorContextLocale().get();
		VendorInfo vendorInfo = DataEvaluator.getVendorDetails(userVendorContext.getVendorInventorySnap().getVendorId());

		Cart cart = new Cart();
	    cart.setUserVendorContext(userVendorContext);
		String cartId = RandomString.nextCartId();
        cart.setCartId(cartId);

		this.cartCache.put(cartId, cart);
        underProcessingOrderDAO.init(cartId, userVendorContext.getVendorInventorySnap().getVendorId(),
                userVendorContext.getVendorInventorySnap().getDeviceDataVersionId(), userVendorContext.getUserId());

		log.debug("Cart [{}], for user [{}] and vendor [{}], initialized and loaded in cache", cartId,
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
                log.debug("Removed barcode [{}] from user [{}]'s cart [{}]", barcode, cartId);
            } else if (existingItemCount == null) {
                cart.getItemCount().put(barcode, newItemCount);
                log.debug("Updated barcode [{}] from user [{}]'s cart [{}] to count [{}] from [{]]", barcode, newItemCount, existingItemCount);
                CartSignalExchange.getInstance().addCartDelta(new CartDelta(cartId, barcode, newItemCount, CartDelta.Operation.ADDED));
            } else {
                cart.getItemCount().put(barcode, newItemCount + existingItemCount);
                CartSignalExchange.getInstance().addCartDelta(new CartDelta(cartId, barcode, newItemCount, CartDelta.Operation.ADDED));
                log.debug("Updated barcode [{}] from user [{}]'s cart [{}] to count [{}] from [{]]", barcode, newItemCount, existingItemCount);
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
                log.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.OPEN.name());
                cart.setStatus(CartStatus.OPEN);
                break;
            case 2:
                log.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.IN_PREPARATION.name());
                cart.setStatus(CartStatus.IN_PREPARATION);
                break;
            case 3:
                log.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.PENDING_USER_REVIEW.name());
                cart.setStatus(CartStatus.PENDING_USER_REVIEW);
                break;
            case 4:
                log.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.PENDING_PAYMENT.name());
                cart.setStatus(CartStatus.PENDING_PAYMENT);
                break;
            case 5:
                log.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.PENDING_DELIVERY.name());
                cart.setStatus(CartStatus.PENDING_DELIVERY);
                break;
            case 6:
                log.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.PENDING_PARTIAL_DELIVERY.name());
                cart.setStatus(CartStatus.PENDING_PARTIAL_DELIVERY);
                break;
            case 7:
                log.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.COMPLETE.name());
                cart.setStatus(CartStatus.COMPLETE);
                break;
            case 8:
                log.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.COMPLETE.name());
                cart.setStatus(CartStatus.DISCARDED);
                break;
            case 9:
                log.debug("Updating cart [{}] status from [{}] to [{}]", cartId, cart.getStatus().name(), CartStatus.COMPLETE.name());
                cart.setStatus(CartStatus.CLOSED);
                //orderDetailDAO.create();
                break;

            default:
                log.debug("Unable to understand status [{}] while updating status for cart [{}] ", statusId, cartId);

        }
        CartSignalExchange.getInstance().addCartDelta(new CartOperation(cartId, cart.getUserVendorContext().getVendorInventorySnap().getVendorId(), cart.getStatus().getValue()));
    }

}
