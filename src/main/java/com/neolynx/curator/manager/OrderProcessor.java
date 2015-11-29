/**
 * 
 */
package com.neolynx.curator.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.BaseResponse;
import com.neolynx.common.model.ErrorCode;
import com.neolynx.common.model.client.InventoryInfo;
import com.neolynx.common.model.client.ItemInfo;
import com.neolynx.common.model.order.CartRequest;
import com.neolynx.common.model.order.ClosureRequest;
import com.neolynx.common.model.order.DeliveryMode;
import com.neolynx.common.model.order.ItemRequest;
import com.neolynx.common.model.order.Response;
import com.neolynx.common.model.order.ResponseUpdate;
import com.neolynx.common.model.order.Status;
import com.neolynx.common.util.Constant;
import com.neolynx.common.util.DataValidator;
import com.neolynx.curator.core.OrderDetail;
import com.neolynx.curator.db.OrderDetailDAO;
import com.neolynx.curator.util.Constants;
import com.neolynx.curator.util.ServerValidator;

/**
 * Created by nitesh.garg on Oct 22, 2015
 *
 */
public class OrderProcessor {
	
	static Logger LOGGER = LoggerFactory.getLogger(OrderProcessor.class);
	
	final OrderDetailDAO orderDetailDAO;
	final PriceEvaluator priceEvaluator;
	final LoadingCache<Long, Long> vendorVersionCache;
	final LoadingCache<String, InventoryInfo> differentialInventoryCache;
	
	/**
	 * @param orderDetailDAO
	 */
	public OrderProcessor(OrderDetailDAO orderDetailDAO,
			LoadingCache<Long, Long> vendorVersionCache,
			LoadingCache<String, InventoryInfo> differentialInventoryCache,
			PriceEvaluator priceEvaluator) {
		super();
		this.orderDetailDAO = orderDetailDAO;
		this.priceEvaluator = priceEvaluator;
		this.vendorVersionCache = vendorVersionCache;
		this.differentialInventoryCache = differentialInventoryCache;
	}

	/**
	 * This function simply takes list of item details which are part of the
	 * cart being sent as part of the order, and generates a comma separated
	 * list of item and their count.
	 * 
	 * @param itemDetails
	 * @return
	 */
	private String getCommaSeparatedItemCodeCount(List<ItemRequest> itemDetails) {
	
		StringBuffer response = new StringBuffer();

		if (CollectionUtils.isNotEmpty(itemDetails)) {
			for (ItemRequest instance : itemDetails) {
				response.append(instance.getItemCode()
						+ Constant.COMMA_SEPARATOR + instance.getCountForInStorePickup()
						+ Constant.COMMA_SEPARATOR + instance.getCountForDelivery() + Constant.COMMA_SEPARATOR);
			}
			return response.substring(0, response.lastIndexOf(Constant.COMMA_SEPARATOR));
		}
			
		return null;
	}

	/**
	 * Processes the order create request.
	 * 
	 * @param cartRequest
	 * @return
	 */
	public Response createOrder(CartRequest cartRequest) {

		Response response = new Response();
		
		/**
		 * Perform the basic data validation checks against the null and empty values.
		 */
		BaseResponse dataValidationResponse = DataValidator.validateFreshCart(cartRequest);
		if(dataValidationResponse.getIsError()) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().addAll(dataValidationResponse.getErrorDetail());
			return response;
		}

		/**
		 * Perform server side checks against data consistency with DB and caches.
		 */
		BaseResponse serverValidationResponse = ServerValidator.validateCart(cartRequest);
		if(serverValidationResponse.getIsError()) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().addAll(serverValidationResponse.getErrorDetail());
			return response;
		}

		/**
		 * Get the latest data version for given vendor in order to evaluate any
		 * pricing changes and communicate the same back to the device. Very
		 * important note that what is being stored in the DB is the
		 * pricing/discounts based on latest data and not what is sent from
		 * device. There is no point storing something which will definitely be
		 * updated later on.
		 */
		Long vendorId = cartRequest.getVendorId();
		Long latestVersionId = this.vendorVersionCache.getIfPresent(vendorId);
		LOGGER.debug("The latest version found for vendor [{}] is [{}], and on device side is [{}].",
				vendorId, latestVersionId, cartRequest.getDeviceDataVersionId());

		OrderDetail orderDetail = new OrderDetail();
		
		
		/**
		 * //TODO Generate a barcode
		 * We may need to update the definition of the generated barcode
		 */
		orderDetail.setGeneratedBarcode("GeneratedBarcode");
		
		//TODO Generate a valid order-id
		orderDetail.setOrderId(System.currentTimeMillis()+"");
		
		orderDetail.setVendorId(vendorId);
		orderDetail.setStatus(Status.CREATED.toString());
		orderDetail.setDeliveryMode(cartRequest.getDeliveryMode().getValue());
		
		if(cartRequest.getDeliveryMode() == DeliveryMode.PARTIAL_DELIVERY || cartRequest.getDeliveryMode() == DeliveryMode.DELIVERY) {
			// Check for null delivery address should already have been conducted
			orderDetail.setDeliverAddressId(cartRequest.getUserDetail().getAddressId());
			orderDetail.setDeliveryMode(cartRequest.getDeliveryMode() == DeliveryMode.PARTIAL_DELIVERY ? Constant.DELIVERY_MODE_PARTIAL_DELIVERY : Constant.DELIVERY_MODE_DELIVERY);
		}
		
		orderDetail.setServerDataVersionId(latestVersionId);
		orderDetail.setDeviceDataVersionId(cartRequest.getDeviceDataVersionId());

		orderDetail.setLastModifiedOn(new Date(System.currentTimeMillis()));
		orderDetail.setCreatedOn(new Date(System.currentTimeMillis()));
		
		/**
		 * If version on device doesn't match the latest version. TODO I think
		 * overall it might be better to store differentials on pricing as well
		 * or only based on pricing/discounts.
		 */
		if (latestVersionId.compareTo(cartRequest.getDeviceDataVersionId()) != 0) {
			response.setUpdateResponseInfo(updateCartForUpdateVersion(vendorId, cartRequest));
		}

		orderDetail.setItemList(getCommaSeparatedItemCodeCount(cartRequest.getItemList()));

		orderDetail.setNetAmount(cartRequest.getNetAmount());
		orderDetail.setTaxAmount(cartRequest.getTaxAmount());
		orderDetail.setTaxableAmount(cartRequest.getTaxableAmount());
		orderDetail.setDiscountAmount(cartRequest.getDiscountAmount());
		
		orderDetail = orderDetailDAO.create(orderDetail);
		
		response.setOrderId(orderDetail.getOrderId());
		response.setOrderStatus(orderDetail.getStatus());
		response.setOrderBarcode(orderDetail.getGeneratedBarcode());

		response.setServerDataVersionId(latestVersionId);
		response.setDeviceDataVersionId(cartRequest.getDeviceDataVersionId());

		return response;
	}
	
	private ResponseUpdate updateCartForUpdateVersion(Long vendorId, CartRequest cartRequest) {
		
		int updateCount = 0;
		ResponseUpdate updateResponseInfo = null;
		
		InventoryInfo inventoryInfo = this.differentialInventoryCache.getIfPresent(vendorId
				+ Constants.CACHE_KEY_SEPARATOR_STRING + cartRequest.getDeviceDataVersionId());
		
		if (inventoryInfo == null || inventoryInfo.getUpdatedItems() == null
				|| inventoryInfo.getUpdatedItems().size() == 0) {
			return updateResponseInfo;
		}
		
		updateResponseInfo = new ResponseUpdate();
		updateResponseInfo.setInventoryResponse(inventoryInfo);
		
		List<ItemRequest> finalItemList = new ArrayList<ItemRequest>();
		List<ItemRequest> updateItemList = new ArrayList<ItemRequest>();
		
		for(ItemRequest instance : cartRequest.getItemList()) {
			String itemCode = instance.getItemCode();
			ItemInfo itemInfo = inventoryInfo.getUpdatedItems().get(itemCode);
			
			if(itemInfo == null) {
				finalItemList.add(instance);
			} else {
				updateCount++;
				ItemRequest itemDetail = new ItemRequest(itemInfo);
				finalItemList.add(itemDetail);
				updateItemList.add(itemDetail);
			}
		}

		if(updateCount > 0) {
			
			cartRequest.setItemList(finalItemList);
			//TODO
			//CartCalculator.calculatePrice(cartRequest);
			
			updateResponseInfo.setOnlyUpdatedItemList(updateItemList);
			
			updateResponseInfo.setNetAmount(cartRequest.getNetAmount());
			updateResponseInfo.setTaxAmount(cartRequest.getTaxAmount());
			updateResponseInfo.setTaxableAmount(cartRequest.getTaxableAmount());
			updateResponseInfo.setDiscountAmount(cartRequest.getDiscountAmount());
			
		}
		
		return updateResponseInfo;
	}
	
	public Response updateOrder(CartRequest cartRequest) {
		
		Response response = new Response();
		
		/**
		 * Perform the basic data validation checks against the null and empty values.
		 */
		BaseResponse dataValidationResponse = DataValidator.validateUpdatedCart(cartRequest);
		if(dataValidationResponse.getIsError()) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().addAll(dataValidationResponse.getErrorDetail());
			return response;
		}
		
		/**
		 * Perform server side checks against data consistency with DB and caches.
		 */
		BaseResponse serverValidationResponse = ServerValidator.validateCartForOrderupdate(cartRequest);
		if(serverValidationResponse.getIsError()) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().addAll(serverValidationResponse.getErrorDetail());
			return response;
		}

		Long vendorId = cartRequest.getVendorId();
		Long latestVersionId = this.vendorVersionCache.getIfPresent(vendorId);
		LOGGER.debug("The latest version found for vendor [{}] is [{}], and on device side is [{}].",
				vendorId, latestVersionId, cartRequest.getDeviceDataVersionId());

		OrderDetail orderDetail = this.orderDetailDAO.findOrderById(cartRequest.getUpdateCart().getOrderId());
		
		if(orderDetail == null) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().add(ErrorCode.ORDER_ID_DOESNT_EXIST_IN_DB);
			return response;
		}
		
		orderDetail.setStatus(Status.UPDATED.toString());
		orderDetail.setLastModifiedOn(new Date(System.currentTimeMillis()));
		
		if (cartRequest.getDeliveryMode() == DeliveryMode.DELIVERY || cartRequest.getDeliveryMode() == DeliveryMode.PARTIAL_DELIVERY) {
			orderDetail.setDeliverAddressId(cartRequest.getUserDetail().getAddressId());
			orderDetail.setDeliveryMode(cartRequest.getDeliveryMode() == DeliveryMode.PARTIAL_DELIVERY ? Constant.DELIVERY_MODE_PARTIAL_DELIVERY
							: Constant.DELIVERY_MODE_DELIVERY);
		} else {
			orderDetail.setDeliverAddressId(null);
			orderDetail.setDeliveryMode(Constant.DELIVERY_MODE_IN_STORE_PICKUP);
		}
		
		if(cartRequest.getUpdateCart().getIsUserDetailUpdated()) {
			/**
			 * Assuming the current captured information of the user will never
			 * change between create and update operations, and address is
			 * anyways copied above. Hence ignoring.
			 */
		}
		
		
		/**
		 * If version on device doesn't match the latest version. But note that
		 * in case client is updating to newer version with differential being
		 * sent down, it's possible that device version is now the previous
		 * latest server version.
		 */
		if(latestVersionId.compareTo(cartRequest.getUpdateCart().getLastKnownServerDataVersionId()) != 0 || cartRequest.getUpdateCart().getIsItemListUpdated()) {
			
			orderDetail.setServerDataVersionId(latestVersionId);
			orderDetail.setDeviceDataVersionId(cartRequest.getDeviceDataVersionId());
			
			response.setUpdateResponseInfo(updateCartForUpdateVersion(vendorId, cartRequest));

		} else {
			// Nothing else changed, good to go.
		}

		orderDetail.setItemList(getCommaSeparatedItemCodeCount(cartRequest.getItemList()));

		orderDetail.setNetAmount(cartRequest.getNetAmount());
		orderDetail.setTaxAmount(cartRequest.getTaxAmount());
		orderDetail.setTaxableAmount(cartRequest.getTaxableAmount());
		orderDetail.setDiscountAmount(cartRequest.getDiscountAmount());
		
		orderDetail = orderDetailDAO.update(orderDetail);
		
		response.setOrderId(orderDetail.getOrderId());
		response.setOrderStatus(orderDetail.getStatus());
		response.setOrderBarcode(orderDetail.getGeneratedBarcode());

		response.setServerDataVersionId(latestVersionId);
		response.setDeviceDataVersionId(cartRequest.getDeviceDataVersionId());

		return response;
	}
	
	public Response completeInStoreProcessing(ClosureRequest closureRequest) {
		
		Response response = new Response();
		OrderDetail orderDetail = this.orderDetailDAO.findOrderById(closureRequest.getOrderId());
		
		if(orderDetail == null) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().add(ErrorCode.ORDER_ID_DOESNT_EXIST_IN_DB);
			return response;
		}
		
		int orderDeliveryMode = orderDetail.getDeliveryMode();
		
		response.setOrderId(closureRequest.getOrderId());
		orderDetail.setLastModifiedOn(new Date(System.currentTimeMillis()));

		if (orderDeliveryMode == Constant.DELIVERY_MODE_PARTIAL_DELIVERY) {
			orderDetail.setStatus(Status.PICKED_PENDING_DELIVERY.toString());
			response.setOrderStatus(Status.PICKED_PENDING_DELIVERY.toString());
		} else if (orderDeliveryMode == Constant.DELIVERY_MODE_DELIVERY) {
			orderDetail.setStatus(Status.PENDING_DELIVERY.toString());
			response.setOrderStatus(Status.PENDING_DELIVERY.toString());
		} else {
			orderDetail.setStatus(Status.COMPLETED.toString());
			response.setOrderStatus(Status.COMPLETED.toString());
		}
		
		this.orderDetailDAO.update(orderDetail);
		return response;
	}

	
	public Response completeDeliveryProcessing(ClosureRequest closureRequest) {
		Response response = new Response();
		OrderDetail orderDetail = this.orderDetailDAO.findOrderById(closureRequest.getOrderId());
		
		if(orderDetail == null) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().add(ErrorCode.ORDER_ID_DOESNT_EXIST_IN_DB);
			return response;
		}
		
		orderDetail.setLastModifiedOn(new Date(System.currentTimeMillis()));
		orderDetail.setStatus(Status.COMPLETED.toString());
		
		this.orderDetailDAO.update(orderDetail);
		
		response.setOrderId(closureRequest.getOrderId());
		response.setOrderStatus(Status.COMPLETED.toString());
		
		return response;
	}
	
}
