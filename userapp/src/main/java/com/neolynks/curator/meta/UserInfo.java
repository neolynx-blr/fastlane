package com.neolynks.curator.meta;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.neolynks.common.model.BaseResponse;

/**
 * Created by nitesh.garg on Dec 27, 2015
 *
 */

@Data
@EqualsAndHashCode (callSuper=false)
public class UserInfo extends BaseResponse {
	
	private static final long serialVersionUID = 3608682167219574096L;

	String userId;

}
