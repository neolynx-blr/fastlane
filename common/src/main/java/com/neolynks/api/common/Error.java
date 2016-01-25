package com.neolynks.api.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by nitesh.garg on 17-Sep-2015
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Error implements Serializable{
	
	private static final long serialVersionUID = 745282020173294382L;

	private String errorCode;
	private String errorMessage;
}
