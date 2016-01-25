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
public class PaymentDetail implements Serializable {

	private static final long serialVersionUID = -4349805592423234370L;
	
	private int paymentMode;
	private int cardType;
	
	private Double amount;
	private Double transactionCharges;

}
