/**
 * 
 */
package com.neolynx.common.model.order;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by nitesh.garg on Oct 22, 2015
 *
 */

@Data
public class ClosureRequest implements Serializable {

	private static final long serialVersionUID = -6648234052901306696L;
	
	private String orderId;
	private PaymentDetail paymentDetail;

}
