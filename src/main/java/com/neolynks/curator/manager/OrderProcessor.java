package com.neolynks.curator.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynks.common.model.client.InventoryInfo;
import com.neolynks.common.model.client.ItemInfo;
import com.neolynks.common.model.client.price.ItemPrice;
import com.neolynks.curator.core.OrderDetail;
import com.neolynks.curator.db.OrderDetailDAO;
import com.neolynks.curator.util.Constants;
import com.neolynks.curator.util.ServerValidator;
import com.neolynks.common.model.BaseResponse;
import com.neolynks.common.model.ErrorCode;
import com.neolynks.common.model.order.CartProcessor;
import com.neolynks.common.model.order.CartRequest;
import com.neolynks.common.model.order.ClosureRequest;
import com.neolynks.common.model.order.DeliveryMode;
import com.neolynks.common.model.order.ItemProcessor;
import com.neolynks.common.model.order.ItemRequest;
import com.neolynks.common.model.order.Response;
import com.neolynks.common.model.order.ResponseUpdate;
import com.neolynks.common.model.order.Status;
import com.neolynks.common.util.Constant;
import com.neolynks.common.util.DataValidator;

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
		
		if(response.getUpdateResponseInfo() != null) {

			orderDetail.setNetAmount(response.getUpdateResponseInfo().getNetAmount());
			orderDetail.setTaxAmount(response.getUpdateResponseInfo().getTaxAmount());
			orderDetail.setTaxableAmount(response.getUpdateResponseInfo().getTaxableAmount());
			orderDetail.setDiscountAmount(response.getUpdateResponseInfo().getDiscountAmount());
			
			List<ItemRequest> finalItemsList = new ArrayList<ItemRequest>();
			Map<String, ItemRequest> barcodeToItemRequestMap = new HashMap<String, ItemRequest>();
			for(ItemRequest itemRequest : cartRequest.getItemList()) {
				barcodeToItemRequestMap.put(itemRequest.getBarcode(), itemRequest);
			}
			
			for(ItemRequest itemRequest : response.getUpdateResponseInfo().getOnlyUpdatedItemList()) {
				barcodeToItemRequestMap.put(itemRequest.getBarcode(), itemRequest);
			}
			
			finalItemsList.addAll(barcodeToItemRequestMap.values());
			orderDetail.setItemList(getCommaSeparatedItemCodeCount(finalItemsList));
			
		} else {
		
			orderDetail.setNetAmount(cartRequest.getNetAmount());
			orderDetail.setTaxAmount(cartRequest.getTaxAmount());
			orderDetail.setTaxableAmount(cartRequest.getTaxableAmount());
			orderDetail.setDiscountAmount(cartRequest.getDiscountAmount());

			orderDetail.setItemList(getCommaSeparatedItemCodeCount(cartRequest.getItemList()));

		}
		
		orderDetail = orderDetailDAO.create(orderDetail);
		
		response.setOrderId(orderDetail.getOrderId());
		response.setOrderStatus(orderDetail.getStatus());
		response.setOrderBarcode(orderDetail.getGeneratedBarcode());

		response.setServerDataVersionId(latestVersionId);
		response.setDeviceDataVersionId(cartRequest.getDeviceDataVersionId());

		return response;
	}
	
	private ResponseUpdate updateCartForUpdateVersion(Long vendorId, CartRequest cartRequest) {

		ResponseUpdate updateResponseInfo = null;
		Boolean isOrderedItemUpdate = Boolean.FALSE;

		InventoryInfo inventoryInfo = this.differentialInventoryCache.getIfPresent(vendorId
				+ Constants.CACHE_KEY_SEPARATOR_STRING + cartRequest.getDeviceDataVersionId());

		if (inventoryInfo == null || inventoryInfo.getUpdatedItems() == null
				|| inventoryInfo.getUpdatedItems().size() == 0) {
			return updateResponseInfo;
		}

		CartProcessor cartProcessor = new CartProcessor(cartRequest);
		List<ItemProcessor> updateItemList = new ArrayList<ItemProcessor>();

		for (ItemRequest instance : cartRequest.getItemList()) {

			String barcode = instance.getBarcode();
			String itemCode = instance.getItemCode();

			ItemInfo itemInfo = inventoryInfo.getUpdatedItems().get(itemCode);

			if (itemInfo != null) {

				isOrderedItemUpdate = Boolean.TRUE;

				ItemProcessor itemProcessor = cartProcessor.getBarcodeItemRequestMap().get(barcode);
				ItemPrice newPrice = itemInfo.getItemPrice();
				ItemPrice oldPrice = itemProcessor.getItemPrice();

				if (oldPrice.compareTo(newPrice) != 0) {
					itemProcessor.setItemPrice(newPrice);
				}

				updateItemList.add(itemProcessor);
				cartProcessor.addItemToCart(itemProcessor);

			}
		}

		if (isOrderedItemUpdate) {
			
			updateResponseInfo = new ResponseUpdate();
			updateResponseInfo.setInventoryResponse(inventoryInfo);

			for (ItemProcessor instance : updateItemList) {
				updateResponseInfo.getOnlyUpdatedItemList().add(instance.generateItemRequest());
			}

			updateResponseInfo.setNetAmount(cartProcessor.getNetAmount());
			updateResponseInfo.setTaxAmount(cartProcessor.getTaxAmount());
			updateResponseInfo.setTaxableAmount(cartProcessor.getTaxableAmount());
			updateResponseInfo.setDiscountAmount(cartProcessor.getDiscountAmount());

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
		
		orderDetail.setServerDataVersionId(latestVersionId);
		orderDetail.setDeviceDataVersionId(cartRequest.getDeviceDataVersionId());

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
		if (latestVersionId.compareTo(cartRequest.getDeviceDataVersionId()) != 0) {
			response.setUpdateResponseInfo(updateCartForUpdateVersion(vendorId, cartRequest));
		}
		
		if(response.getUpdateResponseInfo() != null) {

			orderDetail.setNetAmount(response.getUpdateResponseInfo().getNetAmount());
			orderDetail.setTaxAmount(response.getUpdateResponseInfo().getTaxAmount());
			orderDetail.setTaxableAmount(response.getUpdateResponseInfo().getTaxableAmount());
			orderDetail.setDiscountAmount(response.getUpdateResponseInfo().getDiscountAmount());
			
			List<ItemRequest> finalItemsList = new ArrayList<ItemRequest>();
			Map<String, ItemRequest> barcodeToItemRequestMap = new HashMap<String, ItemRequest>();
			for(ItemRequest itemRequest : cartRequest.getItemList()) {
				barcodeToItemRequestMap.put(itemRequest.getBarcode(), itemRequest);
			}
			
			for(ItemRequest itemRequest : response.getUpdateResponseInfo().getOnlyUpdatedItemList()) {
				barcodeToItemRequestMap.put(itemRequest.getBarcode(), itemRequest);
			}
			
			finalItemsList.addAll(barcodeToItemRequestMap.values());
			orderDetail.setItemList(getCommaSeparatedItemCodeCount(finalItemsList));
			
		} else {
		
			orderDetail.setNetAmount(cartRequest.getNetAmount());
			orderDetail.setTaxAmount(cartRequest.getTaxAmount());
			orderDetail.setTaxableAmount(cartRequest.getTaxableAmount());
			orderDetail.setDiscountAmount(cartRequest.getDiscountAmount());

			orderDetail.setItemList(getCommaSeparatedItemCodeCount(cartRequest.getItemList()));

		}
		
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
