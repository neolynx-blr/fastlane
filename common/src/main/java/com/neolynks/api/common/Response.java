package com.neolynks.api.common;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nitesh.garg on Oct 2, 2015
 */

@Data
public class Response<T> implements Serializable {

	private static final long serialVersionUID = -7602457173741485802L;

	private Boolean isError = Boolean.FALSE;
	private List<ErrorCode> errorDetail = new ArrayList<ErrorCode>();
    private T data;
}
