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
import com.neolynx.common.model.order.CartCalculator;
import com.neolynx.common.model.order.CartDetail;
import com.neolynx.common.model.order.ClosureRequest;
import com.neolynx.common.model.order.DeliveryMode;
import com.neolynx.common.model.order.ItemDetail;
import com.neolynx.common.model.order.Response;
import com.neolynx.common.model.order.Status;
import com.neolynx.common.model.order.UpdateResponseInfo;
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
	private String getCommaSeparatedItemCodeCount(List<ItemDetail> itemDetails) {
	
		StringBuffer response = new StringBuffer();

		if (CollectionUtils.isNotEmpty(itemDetails)) {
			for (ItemDetail instance : itemDetails) {
				response.append(instance.getItemCode()
						+ Constant.COMMA_SEPARATOR + instance.getCount()
						+ Constant.COMMA_SEPARATOR + (instance.getIsMarkedForDelivery() ? "D" : "P") + Constant.COMMA_SEPARATOR);
			}
			return response.substring(0, response.lastIndexOf(Constant.COMMA_SEPARATOR));
		}
			
		return null;
	}

	/**
	 * Processes the order create request.
	 * 
	 * @param cartDetail
	 * @return
	 */
	public Response createOrder(CartDetail cartDetail) {

		Response response = new Response();
		
		/**
		 * Perform the basic data validation checks against the null and empty values.
		 */
		BaseResponse dataValidationResponse = DataValidator.validateFreshCart(cartDetail);
		if(dataValidationResponse.getIsError()) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().addAll(dataValidationResponse.getErrorDetail());
			return response;
		}

		/**
		 * Perform server side checks against data consistency with DB and caches.
		 */
		BaseResponse serverValidationResponse = ServerValidator.validateCart(cartDetail);
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
		Long vendorId = cartDetail.getVendorId();
		Long latestVersionId = this.vendorVersionCache.getIfPresent(vendorId);
		LOGGER.debug("The latest version found for vendor [{}] is [{}], and on device side is [{}].",
				vendorId, latestVersionId, cartDetail.getDeviceDataVersionId());

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
		orderDetail.setDeliveryMode(cartDetail.getDeliveryMode().getValue());
		
		if(cartDetail.getDeliveryMode() == DeliveryMode.PARTIAL_DELIVERY || cartDetail.getDeliveryMode() == DeliveryMode.DELIVERY) {
			// Check for null delivery address should already have been conducted
			orderDetail.setDeliverAddressId(cartDetail.getUserDetail().getAddressId());
			orderDetail.setDeliveryMode(cartDetail.getDeliveryMode() == DeliveryMode.PARTIAL_DELIVERY ? Constant.DELIVERY_MODE_PARTIAL_DELIVERY : Constant.DELIVERY_MODE_DELIVERY);
		}
		
		orderDetail.setServerDataVersionId(latestVersionId);
		orderDetail.setDeviceDataVersionId(cartDetail.getDeviceDataVersionId());

		orderDetail.setLastModifiedOn(new Date(System.currentTimeMillis()));
		orderDetail.setCreatedOn(new Date(System.currentTimeMillis()));
		
		/**
		 * If version on device doesn't match the latest version. TODO I think
		 * overall it might be better to store differentials on pricing as well
		 * or only based on pricing/discounts.
		 */
		if (latestVersionId.compareTo(cartDetail.getDeviceDataVersionId()) != 0) {
			response.setUpdateResponseInfo(updateCartForUpdateVersion(vendorId, cartDetail));
		}

		orderDetail.setItemList(getCommaSeparatedItemCodeCount(cartDetail.getItemList()));

		orderDetail.setNetAmount(cartDetail.getNetAmount());
		orderDetail.setTaxAmount(cartDetail.getTaxAmount());
		orderDetail.setTaxableAmount(cartDetail.getTaxableAmount());
		orderDetail.setDiscountAmount(cartDetail.getDiscountAmount());
		
		orderDetail = orderDetailDAO.create(orderDetail);
		
		response.setOrderId(orderDetail.getOrderId());
		response.setOrderStatus(orderDetail.getStatus());
		response.setOrderBarcode(orderDetail.getGeneratedBarcode());

		response.setServerDataVersionId(latestVersionId);
		response.setDeviceDataVersionId(cartDetail.getDeviceDataVersionId());

		return response;
	}
	
	private UpdateResponseInfo updateCartForUpdateVersion(Long vendorId, CartDetail cartDetail) {
		
		int updateCount = 0;
		UpdateResponseInfo updateResponseInfo = null;
		
		InventoryInfo inventoryInfo = this.differentialInventoryCache.getIfPresent(vendorId
				+ Constants.CACHE_KEY_SEPARATOR_STRING + cartDetail.getDeviceDataVersionId());
		
		if (inventoryInfo == null || inventoryInfo.getUpdatedItems() == null
				|| inventoryInfo.getUpdatedItems().size() == 0) {
			return updateResponseInfo;
		}
		
		updateResponseInfo = new UpdateResponseInfo();
		updateResponseInfo.setInventoryResponse(inventoryInfo);
		
		List<ItemDetail> finalItemList = new ArrayList<ItemDetail>();
		List<ItemDetail> updateItemList = new ArrayList<ItemDetail>();
		
		for(ItemDetail instance : cartDetail.getItemList()) {
			String itemCode = instance.getItemCode();
			ItemInfo itemInfo = inventoryInfo.getUpdatedItems().get(itemCode);
			
			if(itemInfo == null) {
				finalItemList.add(instance);
			} else {
				updateCount++;
				ItemDetail itemDetail = new ItemDetail(itemInfo);
				finalItemList.add(itemDetail);
				updateItemList.add(itemDetail);
			}
		}

		if(updateCount > 0) {
			
			cartDetail.setItemList(finalItemList);
			CartCalculator.calculatePrice(cartDetail);
			
			updateResponseInfo.setOnlyUpdatedItemList(updateItemList);
			
			updateResponseInfo.setNetAmount(cartDetail.getNetAmount());
			updateResponseInfo.setTaxAmount(cartDetail.getTaxAmount());
			updateResponseInfo.setTaxableAmount(cartDetail.getTaxableAmount());
			updateResponseInfo.setDiscountAmount(cartDetail.getDiscountAmount());
			
		}
		
		return updateResponseInfo;
	}
	
	public Response updateOrder(CartDetail cartDetail) {
		
		Response response = new Response();
		
		/**
		 * Perform the basic data validation checks against the null and empty values.
		 */
		BaseResponse dataValidationResponse = DataValidator.validateUpdatedCart(cartDetail);
		if(dataValidationResponse.getIsError()) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().addAll(dataValidationResponse.getErrorDetail());
			return response;
		}
		
		/**
		 * Perform server side checks against data consistency with DB and caches.
		 */
		BaseResponse serverValidationResponse = ServerValidator.validateCartForOrderupdate(cartDetail);
		if(serverValidationResponse.getIsError()) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().addAll(serverValidationResponse.getErrorDetail());
			return response;
		}

		Long vendorId = cartDetail.getVendorId();
		Long latestVersionId = this.vendorVersionCache.getIfPresent(vendorId);
		LOGGER.debug("The latest version found for vendor [{}] is [{}], and on device side is [{}].",
				vendorId, latestVersionId, cartDetail.getDeviceDataVersionId());

		OrderDetail orderDetail = this.orderDetailDAO.findOrderById(cartDetail.getUpdateCart().getOrderId());
		
		if(orderDetail == null) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().add(ErrorCode.ORDER_ID_DOESNT_EXIST_IN_DB);
			return response;
		}
		
		orderDetail.setStatus(Status.UPDATED.toString());
		orderDetail.setLastModifiedOn(new Date(System.currentTimeMillis()));
		
		if (cartDetail.getDeliveryMode() == DeliveryMode.DELIVERY || cartDetail.getDeliveryMode() == DeliveryMode.PARTIAL_DELIVERY) {
			orderDetail.setDeliverAddressId(cartDetail.getUserDetail().getAddressId());
			orderDetail.setDeliveryMode(cartDetail.getDeliveryMode() == DeliveryMode.PARTIAL_DELIVERY ? Constant.DELIVERY_MODE_PARTIAL_DELIVERY
							: Constant.DELIVERY_MODE_DELIVERY);
		} else {
			orderDetail.setDeliverAddressId(null);
			orderDetail.setDeliveryMode(Constant.DELIVERY_MODE_IN_STORE_PICKUP);
		}
		
		if(cartDetail.getUpdateCart().getIsUserDetailUpdated()) {
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
		if(latestVersionId.compareTo(cartDetail.getUpdateCart().getLastKnownServerDataVersionId()) != 0 || cartDetail.getUpdateCart().getIsItemListUpdated()) {
			
			orderDetail.setServerDataVersionId(latestVersionId);
			orderDetail.setDeviceDataVersionId(cartDetail.getDeviceDataVersionId());
			
			response.setUpdateResponseInfo(updateCartForUpdateVersion(vendorId, cartDetail));

		} else {
			// Nothing else changed, good to go.
		}

		orderDetail.setItemList(getCommaSeparatedItemCodeCount(cartDetail.getItemList()));

		orderDetail.setNetAmount(cartDetail.getNetAmount());
		orderDetail.setTaxAmount(cartDetail.getTaxAmount());
		orderDetail.setTaxableAmount(cartDetail.getTaxableAmount());
		orderDetail.setDiscountAmount(cartDetail.getDiscountAmount());
		
		orderDetail = orderDetailDAO.update(orderDetail);
		
		response.setOrderId(orderDetail.getOrderId());
		response.setOrderStatus(orderDetail.getStatus());
		response.setOrderBarcode(orderDetail.getGeneratedBarcode());

		response.setServerDataVersionId(latestVersionId);
		response.setDeviceDataVersionId(cartDetail.getDeviceDataVersionId());

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
