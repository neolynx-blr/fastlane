package com.neolynx.curator.util;

import com.neolynx.common.model.BaseResponse;
import com.neolynx.common.model.order.CartRequest;

/**
 * Created by nitesh.garg on Oct 25, 2015
 *
 */
public class ServerValidator {

	public static BaseResponse validateCart(CartRequest cart) {

		BaseResponse response = new BaseResponse();

		/**
		 * TODO
		 * 
		 * Idea here is to validate the vendor/version/item-codes received in
		 * the cart to be valid.
		 */

		return response;

	}

	public static BaseResponse validateCartForOrderupdate(CartRequest cart) {
		BaseResponse response = new BaseResponse();

		/**
		 * TODO
		 * 
		 * Idea here is to validate the order-id that must be present already
		 * for update call.
		 */

		return response;
	}

}
