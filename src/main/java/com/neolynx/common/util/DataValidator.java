/**
 * 
 */
package com.neolynx.common.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.neolynx.common.model.BaseResponse;
import com.neolynx.common.model.ErrorCode;
import com.neolynx.common.model.order.CartDetail;

/**
 * Created by nitesh.garg on Oct 25, 2015
 *
 */
public class DataValidator {

	public static BaseResponse validateCart(CartDetail cart) {

		BaseResponse response = new BaseResponse();

		if (LongUtilsCustom.isEmpty(cart.getVendorId())) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().add(ErrorCode.INVALID_VENDOR_ID);
		}

		if (LongUtilsCustom.isEmpty(cart.getDeviceDataVersionId())) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().add(ErrorCode.UNKNOWN_INVENTORY_VERSION_ON_DEVICE);
		}

		if (CollectionUtils.isEmpty(cart.getItemListForPickup())
				&& CollectionUtils.isEmpty(cart.getItemListForDelivery())) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().add(ErrorCode.MISSING_ITEMS_IN_CART);
		}

		if (!StringUtils.isEmpty(cart.getOrderId())
				&& (!cart.getIsDeliveryItemListUpdated() && !cart.getIsPickUpItemListUpdated())) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().add(ErrorCode.MISSING_ITEMS_TO_BE_UPDATED_IN_CART);
		}

		if (CollectionUtils.isNotEmpty(cart.getItemListForDelivery())
				&& (cart.getUserDetail() == null || StringUtils.isEmpty(cart.getUserDetail().getUserId()))) {
			response.setIsError(Boolean.TRUE);
			response.getErrorDetail().add(ErrorCode.MISSING_USER_DETAIL_FOR_DELIVERY);
		}

		return response;

	}

}
