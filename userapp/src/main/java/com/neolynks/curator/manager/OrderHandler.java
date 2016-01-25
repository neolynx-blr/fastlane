package com.neolynks.curator.manager;

import java.util.Map;

import com.google.gson.Gson;
import com.neolynks.api.common.OrderStatus;
import com.neolynks.app.notification.UserAppNotificationSignal;
import com.neolynks.curator.exception.InvalidOrderIdException;
import com.neolynks.curator.exception.InvalidStatusIdException;
import com.neolynks.curator.exception.InvalidStatusTransitionException;
import com.neolynks.signal.CartSignalExchange;
import com.neolynks.api.common.UserVendorContext;
import com.neolynks.api.userapp.price.OrderPrice;
import com.neolynks.curator.cache.order.OrderCache;
import com.neolynks.curator.util.UserContextThreadLocal;
import com.neolynks.dao.OrderDetailDAO;
import com.neolynks.signal.ISignalProcessor;
import com.neolynks.signal.WorkerSignalExchange;
import com.neolynks.signal.dto.CartDelta;
import com.neolynks.signal.dto.CartOperation;
import com.neolynks.model.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.extern.slf4j.Slf4j;

import com.google.common.cache.LoadingCache;
import com.neolynks.curator.meta.DataEvaluator;
import com.neolynks.curator.meta.VendorInfo;
import com.neolynks.curator.dto.Order;
import com.neolynks.curator.util.RandomString;
import org.apache.commons.lang3.SerializationUtils;

/**
 * Created by nitesh.garg on Dec 26, 2015
 * @author nishantgupta
 * @author nitesh.garg
 *
 */

@Data
@Slf4j
@AllArgsConstructor
public class OrderHandler {

    private final static Gson gson = new Gson();

	private final OrderDetailDAO orderDetailDAO;
	private final OrderCache orderCache;
    private final PriceEvaluator priceEvaluator;

	private final LoadingCache<Long, Long> vendorVersionCache;
    private final WorkerSignalExchange workerSignalExchange;
    private final CartSignalExchange cartSignalExchange;

    /***************************************/

    public static class WorkerSignalProcessor implements ISignalProcessor {

        //TODO: Pending implementation
        @Override
        public void process(byte[] message) {
            CartOperation cartOperation = (CartOperation) SerializationUtils.deserialize(message);
            UserAppNotificationSignal.getUserAppNotificationSignal().publishUserAppNotification("userId",
                    cartOperation.getCartId(), cartOperation.getOrderStatus());
        }
    }

    /******************************/


    public String initializeCart() {
        UserVendorContext userVendorContext = UserContextThreadLocal.getUserVendorContextLocale().get();
		VendorInfo vendorInfo = DataEvaluator.getVendorDetails(userVendorContext.getVendorInventorySnap().getVendorId());

		Order order = new Order();
	    order.setUserVendorContext(userVendorContext);
		String orderId = RandomString.nextCartId();
        order.setCartId(orderId);

		this.orderCache.updateCache(orderId, order);

		log.debug("Cart [{}], for user [{}] and vendor [{}], initialized and loaded in cache", orderId,
                userVendorContext.getUserId(), vendorInfo.getVendorId());
		return orderId;
	}
	
	public void setToCart(String orderId, Map<String, Integer> itemCount) throws InvalidOrderIdException {
		Order order = this.orderCache.get(orderId);
		
		if(order == null || !OrderStatus.OPEN.equals(order.getStatus())) {
            throw InvalidOrderIdException.getDefaut();
		}

        for (String barcode : itemCount.keySet()) {
            Integer existingItemCount = order.getItemCount().get(barcode);
            Integer newItemCount = itemCount.get(barcode);
            if (newItemCount == 0) {
                order.getItemCount().remove(barcode);
                cartSignalExchange.publishCartDelta(new CartDelta(orderId, barcode, 0, CartDelta.Operation.REMOVED));
                log.debug("Removed barcode [{}] from user [{}]'s cart [{}]", barcode, orderId);
            } else if (existingItemCount == null) {
                order.getItemCount().put(barcode, newItemCount);
                log.debug("Updated barcode [{}] from user [{}]'s cart [{}] to count [{}] from [{]]", barcode, newItemCount, existingItemCount);
                cartSignalExchange.publishCartDelta(new CartDelta(orderId, barcode, newItemCount, CartDelta.Operation.ADDED));
            } else {
                order.getItemCount().put(barcode, newItemCount + existingItemCount);
                cartSignalExchange.publishCartDelta(new CartDelta(orderId, barcode, newItemCount, CartDelta.Operation.ADDED));
                log.debug("Updated barcode [{}] from user [{}]'s cart [{}] to count [{}] from [{]]", barcode, newItemCount, existingItemCount);
            }
        }
        this.orderCache.updateCache(orderId, order);
	}

    //TODO: Transaction handling is missing.
    public void orderClosed(String orderId, Map<String, Integer> completeItemCount) throws InvalidOrderIdException {
        Order order = this.orderCache.get(orderId);

        if(order == null || !OrderStatus.OPEN.equals(order.getStatus())) {
            throw InvalidOrderIdException.getDefaut();
        }

        for (String barcode : completeItemCount.keySet()) {
            Integer existingItemCount = order.getItemCount().get(barcode);
            Integer newItemCount = completeItemCount.get(barcode);
            if (existingItemCount == null) {
                log.debug("Updated barcode [{}] from user [{}]'s cart [{}] to count [{}] from [{]]", barcode, newItemCount, existingItemCount);
                cartSignalExchange.publishCartDelta(new CartDelta(orderId, barcode, newItemCount, CartDelta.Operation.ADDED));
            } else if(newItemCount != existingItemCount && newItemCount > existingItemCount) {
                cartSignalExchange.publishCartDelta(new CartDelta(orderId, barcode, newItemCount - existingItemCount, CartDelta.Operation.ADDED));
                log.debug("Updated barcode [{}] from user [{}]'s cart [{}] to count [{}] from [{]]", barcode, newItemCount, existingItemCount);
            }
        }
        order.setItemCount(completeItemCount);
        order.setStatus(OrderStatus.ORDER_PLACED);
        cartSignalExchange.publishCartOperation(new CartOperation(orderId, order.getUserVendorContext().getVendorInventorySnap().getVendorId(), order.getStatus()));
        this.orderCache.updateCache(orderId, order);

        OrderPrice orderPrice = priceEvaluator.getOrderPrice(order);
        OrderDetail orderDetail = OrderDetail.builder().orderId(orderId)
                .itemList(gson.toJson(order.getItemCount()))
                .discountAmount(orderPrice.getDiscountAmount())
                .netAmount(orderPrice.getNetAmount())
                .taxableAmount(orderPrice.getTaxableAmount())
                .taxAmount(orderPrice.getTaxAmount())
                .status(order.getStatus().getValue())
                .vendorId(order.getUserVendorContext().getVendorInventorySnap().getVendorId())
                .versionId(order.getUserVendorContext().getVendorInventorySnap().getDeviceDataVersionId())
                .userId(order.getUserVendorContext().getUserId()).build();
        orderDetailDAO.create(orderDetail);
    }

    public void setOrderStatus(String orderId, Integer statusId) throws InvalidOrderIdException,
            InvalidStatusIdException,
            InvalidStatusTransitionException {

        Order order = this.orderCache.get(orderId);
        if (order == null) {
            throw InvalidOrderIdException.getDefaut();
        }

        OrderStatus nextOrderStatus = OrderStatus.getStatus(statusId);
        if(nextOrderStatus == null){
            throw new InvalidStatusIdException(orderId, statusId);
        }
        if(!order.getStatus().getPossibleNext().contains(nextOrderStatus)){
            throw new InvalidStatusTransitionException(orderId, order.getStatus(), nextOrderStatus);
        }
        log.debug("Updating cart [{}] status from [{}] to [{}]", orderId, order.getStatus().name(), nextOrderStatus.name());
        order.setStatus(nextOrderStatus);

        cartSignalExchange.publishCartOperation(new CartOperation(orderId, order.getUserVendorContext().getVendorInventorySnap().getVendorId(), order.getStatus()));
        this.orderCache.updateCache(orderId, order);
    }

}
