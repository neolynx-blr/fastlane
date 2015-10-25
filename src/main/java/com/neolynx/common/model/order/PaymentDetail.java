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
public class PaymentDetail implements Serializable {

	private static final long serialVersionUID = -4349805592423234370L;
	
	private int paymentMode;
	private int cardType;
	
	private Double amount;
	private Double transactionCharges;

}
