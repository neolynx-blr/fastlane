/**
 * 
 */
package com.neolynx.common.model.order;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.neolynx.common.model.BaseResponse;

/**
 * Created by nitesh.garg on Oct 22, 2015
 *
 */

@Data
@EqualsAndHashCode(callSuper=false)
public class Response extends BaseResponse {

	private static final long serialVersionUID = 8768826874201574453L;

	/**
	 * If things are okay, generate the order details and send back in response.
	 */
	private String orderId;
	private String orderStatus;
	private String orderBarcode;

	private Long serverDataVersionId;
	private Long deviceDataVersionId;

	/**
	 * Collects all information which is related to any changes in the inventory
	 * w.r.t. what is available on the user device at the time of placing the
	 * order.
	 */
	UpdateResponseInfo updateResponseInfo;
}
