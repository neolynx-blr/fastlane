package com.neolynks.api.common;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nitesh.garg on Oct 2, 2015
 */

@Data
public class Response<T> implements Serializable {

    private Response(boolean isError){
        this.isError = isError;
    }

	private static final long serialVersionUID = -7602457173741485802L;

	private final Boolean isError;
	private List<ErrorCode> errorDetail = new ArrayList<ErrorCode>();
    private T data;

    public static <E> Response<E> getSuccessResponse(E t){
        Response<E> response = new Response<>(false);
        response.setData(t);
        return response;
    }

    public static Response getFailureResponse(List<ErrorCode> errorCodes){
        Response response = new Response<>(true);
        response.setErrorDetail(errorCodes);
        return response;
    }

    public static Response getFailureResponse(ErrorCode... errorCodes){
        Response response = new Response<>(true);
        response.setErrorDetail(Arrays.asList(errorCodes));
        return response;
    }
}
