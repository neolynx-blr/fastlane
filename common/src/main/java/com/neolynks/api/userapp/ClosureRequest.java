/**
 * 
 */
package com.neolynks.api.userapp;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by nitesh.garg on Oct 22, 2015
 *
 */

@Data
public class ClosureRequest implements Serializable {

	private static final long serialVersionUID = -6648234052901306696L;
	
	private Long orderId;
	private PaymentDetail paymentDetail;

}
