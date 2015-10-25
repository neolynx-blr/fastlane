/**
 * 
 */
package com.neolynx.curator.manager;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.BaseResponse;
import com.neolynx.common.model.ErrorCode;
import com.neolynx.common.model.InventoryResponse;
import com.neolynx.common.model.ResponseAudit;
import com.neolynx.common.model.order.CartCalculator;
import com.neolynx.common.model.order.CartDetail;
import com.neolynx.common.model.order.ClosureRequest;
import com.neolynx.common.model.order.ItemDetail;
import com.neolynx.common.model.order.Response;
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
	final LoadingCache<String, InventoryResponse> differentialInventoryCache;
	
	/**
	 * @param orderDetailDAO
	 */
	public OrderProcessor(OrderDetailDAO orderDetailDAO,
			LoadingCache<Long, Long> vendorVersionCache,
			LoadingCache<String, InventoryResponse> differentialInventoryCache,
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
						+ Constant.COMMA_SEPARATOR);
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
		BaseResponse dataValidationResponse = DataValidator.validateCart(cartDetail);
		if(dataValidationResponse.getIsError()) {
			response.setIsError(Boolean.TRUE);
			response.setErrorDetail(dataValidationResponse.getErrorDetail());
			return response;
		}

		/**
		 * Perform server side checks against data consistency with DB and caches.
		 */
		BaseResponse serverValidationResponse = ServerValidator.validateCart(cartDetail);
		if(serverValidationResponse.getIsError()) {
			response.setIsError(Boolean.TRUE);
			response.setErrorDetail(serverValidationResponse.getErrorDetail());
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
		
		//TODO Generate a barcode
		orderDetail.setGeneratedBarcode("GeneratedBarcode");
		
		//TODO Generate a valid order-id
		orderDetail.setOrderId("1234567890L");
		orderDetail.setStatus(Status.CREATED.toString());
		
		orderDetail.setVendorId(vendorId);
		orderDetail.setDeliverAddressId(cartDetail.getUserDetail().getAddressId());
		
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
			/**
			 * TODO This needs to be combined for optimal updates.
			 */
			List<ItemDetail> updatedPickUpList = this.priceEvaluator.updateItemPricingToLatestVersion(vendorId, cartDetail.getDeviceDataVersionId(), cartDetail.getItemListForPickup());
			List<ItemDetail> updatedDeliveryList = this.priceEvaluator.updateItemPricingToLatestVersion(vendorId, cartDetail.getDeviceDataVersionId(), cartDetail.getItemListForDelivery());
			
			//If anything changed on pickup list
			if(CollectionUtils.isNotEmpty(updatedPickUpList)) {
				cartDetail.setItemListForPickup(updatedPickUpList);
				for(ItemDetail instance : updatedPickUpList) {
					if(instance.getIsPricingChanged()) {
						response.getOnlyUpdatedItemListForPickup().add(instance);
					}
				}
			}

			// If anything changed on delivery list
			if(CollectionUtils.isNotEmpty(updatedDeliveryList)) {
				cartDetail.setItemListForDelivery(updatedDeliveryList);
				for(ItemDetail instance : updatedDeliveryList) {
					if(instance.getIsPricingChanged()) {
						response.getOnlyUpdatedItemListForDelivery().add(instance);
					}
				}
			}

			// Recalculate cart pricing
			if (CollectionUtils.isNotEmpty(updatedDeliveryList)
					|| CollectionUtils.isNotEmpty(updatedPickUpList)) {
				CartCalculator.calculatePrice(cartDetail);
			}
		}

		orderDetail.setItemList(getCommaSeparatedItemCodeCount(cartDetail.getItemListForPickup()));
		orderDetail.setItemListForDelivery(getCommaSeparatedItemCodeCount(cartDetail.getItemListForDelivery()));

		orderDetail.setTaxAmount(cartDetail.getTaxAmount());
		orderDetail.setNetAmount(cartDetail.getNetAmount());
		orderDetail.setDiscountAmount(cartDetail.getDiscountAmount());
		
		orderDetail = orderDetailDAO.create(orderDetail);
		
		response.setOrderId(orderDetail.getOrderId());
		response.setOrderStatus(orderDetail.getStatus());
		response.setOrderBarcode(orderDetail.getGeneratedBarcode());

		response.setServerDataVersionId(latestVersionId);
		response.setDeviceDataVersionId(cartDetail.getDeviceDataVersionId());

		if (latestVersionId.compareTo(cartDetail.getDeviceDataVersionId()) != 0) {
			InventoryResponse inventoryResponse = this.differentialInventoryCache
					.getIfPresent(vendorId
							+ Constants.CACHE_KEY_SEPARATOR_STRING
							+ latestVersionId);
			response.setInventoryResponse(inventoryResponse);

			response.setTaxAmount(cartDetail.getTaxAmount());
			response.setNetAmount(cartDetail.getNetAmount());
			response.setDiscountAmount(cartDetail.getDiscountAmount());

		}

		return response;
	}
	
	public Response updateOrder(CartDetail cartDetail) {
		
		Response response = new Response();
		
		/**
		 * Perform the basic data validation checks against the null and empty values.
		 */
		BaseResponse dataValidationResponse = DataValidator.validateCart(cartDetail);
		if(dataValidationResponse.getIsError()) {
			response.setIsError(Boolean.TRUE);
			response.setErrorDetail(dataValidationResponse.getErrorDetail());
			return response;
		}
		
		/**
		 * Perform server side checks against data consistency with DB and caches.
		 */
		BaseResponse serverValidationResponse = ServerValidator.validateCartForOrderupdate(cartDetail);
		if(serverValidationResponse.getIsError()) {
			response.setIsError(Boolean.TRUE);
			response.setErrorDetail(serverValidationResponse.getErrorDetail());
			return response;
		}

		Long vendorId = cartDetail.getVendorId();
		Long latestVersionId = this.vendorVersionCache.getIfPresent(vendorId);
		LOGGER.debug("The latest version found for vendor [{}] is [{}], and on device side is [{}].",
				vendorId, latestVersionId, cartDetail.getDeviceDataVersionId());

		OrderDetail orderDetail = this.orderDetailDAO.findOrderById(cartDetail.getOrderId());
		
		if(orderDetail == null) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().add(ErrorCode.ORDER_ID_DOESNT_EXIST_IN_DB);
			return response;
		}
		
		orderDetail.setStatus(Status.UPDATED.toString());
		orderDetail.setDeliverAddressId(cartDetail.getUserDetail().getAddressId());
		
		orderDetail.setServerDataVersionId(latestVersionId);
		orderDetail.setDeviceDataVersionId(cartDetail.getDeviceDataVersionId());

		orderDetail.setLastModifiedOn(new Date(System.currentTimeMillis()));

		/**
		 * If version on device doesn't match the latest version. TODO I think
		 * overall it might be better to store differentials on pricing as well
		 * or only based on pricing/discounts.
		 */
		if (latestVersionId.compareTo(cartDetail.getDeviceDataVersionId()) != 0) {
			/**
			 * TODO This needs to be combined for optimal updates.
			 */
			List<ItemDetail> updatedPickUpList = this.priceEvaluator.updateItemPricingToLatestVersion(vendorId, cartDetail.getDeviceDataVersionId(), cartDetail.getItemListForPickup());
			List<ItemDetail> updatedDeliveryList = this.priceEvaluator.updateItemPricingToLatestVersion(vendorId, cartDetail.getDeviceDataVersionId(), cartDetail.getItemListForDelivery());
			
			//If anything changed on pickup list
			if(CollectionUtils.isNotEmpty(updatedPickUpList)) {
				cartDetail.setItemListForPickup(updatedPickUpList);
				for(ItemDetail instance : updatedPickUpList) {
					if(instance.getIsPricingChanged()) {
						response.getOnlyUpdatedItemListForPickup().add(instance);
					}
				}
			}

			// If anything changed on delivery list
			if(CollectionUtils.isNotEmpty(updatedDeliveryList)) {
				cartDetail.setItemListForDelivery(updatedDeliveryList);
				for(ItemDetail instance : updatedDeliveryList) {
					if(instance.getIsPricingChanged()) {
						response.getOnlyUpdatedItemListForDelivery().add(instance);
					}
				}
			}

			// Recalculate cart pricing
			if (CollectionUtils.isNotEmpty(updatedDeliveryList)
					|| CollectionUtils.isNotEmpty(updatedPickUpList)) {
				CartCalculator.calculatePrice(cartDetail);
			}
			
		} 
		
		/**
		 * Update the comma separated list of items and their count in case any
		 * of those has changed as part of this update request.
		 */
		if(cartDetail.getIsDeliveryItemListUpdated()) {
			orderDetail.setItemListForDelivery(getCommaSeparatedItemCodeCount(cartDetail.getItemListForDelivery()));
		}
		
		if(cartDetail.getIsPickUpItemListUpdated()) {
			orderDetail.setItemList(getCommaSeparatedItemCodeCount(cartDetail.getItemListForPickup()));
		}
		
		orderDetail.setTaxAmount(cartDetail.getTaxAmount());
		orderDetail.setNetAmount(cartDetail.getNetAmount());
		orderDetail.setDiscountAmount(cartDetail.getDiscountAmount());
		
		orderDetail = this.orderDetailDAO.update(orderDetail);

		response.setOrderId(orderDetail.getOrderId());
		response.setOrderStatus(orderDetail.getStatus());
		response.setOrderBarcode(orderDetail.getGeneratedBarcode());

		response.setServerDataVersionId(latestVersionId);
		response.setDeviceDataVersionId(cartDetail.getDeviceDataVersionId());

		if (latestVersionId > cartDetail.getDeviceDataVersionId()) {
			
			InventoryResponse inventoryResponse = this.differentialInventoryCache
					.getIfPresent(vendorId
							+ Constants.CACHE_KEY_SEPARATOR_STRING
							+ latestVersionId);
			response.setInventoryResponse(inventoryResponse);
			
			response.setTaxAmount(cartDetail.getTaxAmount());
			response.setNetAmount(cartDetail.getNetAmount());
			response.setDiscountAmount(cartDetail.getDiscountAmount());
			
		}
		
		return response;
	}
	
	public Response completeOrder(ClosureRequest closureRequest) {
		
		Response response = new Response();
		
		OrderDetail orderDetail = this.orderDetailDAO.findOrderById(closureRequest.getOrderId());
		
		if(orderDetail == null) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().add(ErrorCode.ORDER_ID_DOESNT_EXIST_IN_DB);
			return response;
		}
		
		orderDetail.setStatus(Status.PAID_FOR.toString());
		orderDetail.setLastModifiedOn(new Date(System.currentTimeMillis()));
		
		this.orderDetailDAO.update(orderDetail);
		
		response.setOrderId(closureRequest.getOrderId());
		response.setOrderStatus(Status.PAID_FOR.toString());
		
		return response;
	}
	
}
