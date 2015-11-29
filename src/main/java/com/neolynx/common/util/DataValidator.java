/**
 * 
 */
package com.neolynx.common.util;

import org.apache.commons.collections4.CollectionUtils;

import com.neolynx.common.model.BaseResponse;
import com.neolynx.common.model.ErrorCode;
import com.neolynx.common.model.order.CartRequest;

/**
 * Created by nitesh.garg on Oct 25, 2015
 *
 */
public class DataValidator {

	public static BaseResponse validateFreshCart(CartRequest cart) {

		BaseResponse response = new BaseResponse();

		if (LongUtilsCustom.isEmpty(cart.getVendorId())) {
			response.getErrorDetail().add(ErrorCode.INVALID_VENDOR_ID);
		}

		if (LongUtilsCustom.isEmpty(cart.getDeviceDataVersionId())) {
			response.getErrorDetail().add(ErrorCode.UNKNOWN_INVENTORY_VERSION_ON_DEVICE);
		} else if (cart.getDeviceDataVersionId() < 0L) {
			response.getErrorDetail().add(ErrorCode.EXPIRED_DATA_VERSION);
		}

		if (cart.getDeliveryMode().getValue() == Constant.DELIVERY_MODE_DELIVERY
				|| cart.getDeliveryMode().getValue() == Constant.DELIVERY_MODE_PARTIAL_DELIVERY) {

			if (cart.getUserDetail() == null || cart.getUserDetail().getUserId() == null
					|| cart.getUserDetail().getAddressId() == null) {
				response.getErrorDetail().add(ErrorCode.MISSING_USER_DETAILS_FOR_DELIVERY);
			}

		}

		if (cart.getDeliveryMode() == null) {
			response.getErrorDetail().add(ErrorCode.MISSING_USER_DETAILS_FOR_DELIVERY);
		}

		if (CollectionUtils.isEmpty(cart.getItemList())) {
			response.getErrorDetail().add(ErrorCode.MISSING_ITEMS_IN_CART);
		} else {

			int itemCount = 0;
			int totalCount = 0;

			// TODO Validate delivery mode being correct

			if (cart.getItemCount() != itemCount) {
				response.getErrorDetail().add(ErrorCode.INVALID_COUNT_OF_ITEM_IN_ORDER);
			}

			if (cart.getTotalCount() != totalCount) {
				response.getErrorDetail().add(ErrorCode.INVALID_TOTAL_COUNT_OF_ITEM_IN_ORDER);
			}

		}

		if (CollectionUtils.isNotEmpty(response.getErrorDetail())) {
			response.setIsError(Boolean.TRUE);
		}

		return response;

	}

	public static BaseResponse validateUpdatedCart(CartRequest cart) {

		BaseResponse response = new BaseResponse();

		if (cart.getUpdateCart() == null) {
			response.getErrorDetail().add(ErrorCode.MISSING_UPDATE_CART_DETAILS);
		}

		if (cart.getUpdateCart().getOrderId() == null) {
			response.getErrorDetail().add(ErrorCode.MISSING_ORDER_ID);
		}

		if (cart.getUpdateCart().getIsItemListUpdated() == Boolean.FALSE
				&& cart.getUpdateCart().getIsUserDetailUpdated() == Boolean.FALSE) {
			response.getErrorDetail().add(ErrorCode.MISSING_UPDATE_OPERATION_REASON);
		}

		if (cart.getUpdateCart().getLastKnownServerDataVersionId() == null) {
			response.getErrorDetail().add(ErrorCode.MISSING_OR_INVALID_LAST_SEEN_SERVER_DATA_VERSION_ID);
		}

		if (LongUtilsCustom.isEmpty(cart.getVendorId())) {
			response.getErrorDetail().add(ErrorCode.INVALID_VENDOR_ID);
		}

		if (LongUtilsCustom.isEmpty(cart.getDeviceDataVersionId())) {
			response.getErrorDetail().add(ErrorCode.UNKNOWN_INVENTORY_VERSION_ON_DEVICE);
		} else if (cart.getDeviceDataVersionId() < 0L) {
			response.getErrorDetail().add(ErrorCode.EXPIRED_DATA_VERSION);
		}

		if (cart.getDeliveryMode().getValue() == Constant.DELIVERY_MODE_DELIVERY
				|| cart.getDeliveryMode().getValue() == Constant.DELIVERY_MODE_PARTIAL_DELIVERY) {

			if (cart.getUserDetail() == null || cart.getUserDetail().getUserId() == null
					|| cart.getUserDetail().getAddressId() == null) {
				response.getErrorDetail().add(ErrorCode.MISSING_USER_DETAILS_FOR_DELIVERY);
			}

		}

		if (cart.getDeliveryMode() == null) {
			response.getErrorDetail().add(ErrorCode.MISSING_USER_DETAILS_FOR_DELIVERY);
		}

		if (CollectionUtils.isEmpty(cart.getItemList())) {
			response.getErrorDetail().add(ErrorCode.MISSING_ITEMS_IN_CART);
		} else {

			int itemCount = 0;
			int totalCount = 0;

			// TODO Validate delivery mode being correct

			if (cart.getItemCount() != itemCount) {
				response.getErrorDetail().add(ErrorCode.INVALID_COUNT_OF_ITEM_IN_ORDER);
			}

			if (cart.getTotalCount() != totalCount) {
				response.getErrorDetail().add(ErrorCode.INVALID_TOTAL_COUNT_OF_ITEM_IN_ORDER);
			}

		}

		if (CollectionUtils.isNotEmpty(response.getErrorDetail())) {
			response.setIsError(Boolean.TRUE);
		}

		return response;

	}
}
