package com.neolynx.common.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by nitesh.garg on 17-Sep-2015
 */

@Data
public class Error implements Serializable{
	
	private static final long serialVersionUID = 745282020173294382L;

	String errorCode;
	String errorMessage;
	
	public Error(String errorCode, String errorMessage) {
		super();
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public Error() {
	}
	
}
