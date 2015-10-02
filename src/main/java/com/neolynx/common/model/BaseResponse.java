package com.neolynx.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Created by nitesh.garg on Oct 2, 2015
 */

@Data
public class BaseResponse implements Serializable {

	private static final long serialVersionUID = -7602457173741485802L;

	Boolean isError = Boolean.TRUE;
	List<Error> errorDetails = new ArrayList<Error>();

}
