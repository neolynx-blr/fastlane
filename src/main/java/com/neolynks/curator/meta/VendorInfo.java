package com.neolynks.curator.meta;

import com.neolynks.common.model.BaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by nitesh.garg on Dec 27, 2015
 *
 */

@Data
@EqualsAndHashCode (callSuper=false)
public class VendorInfo extends BaseResponse {
	
	private static final long serialVersionUID = 3608682167219574096L;

	Long vendorId;
	String vendorAbbr;
	Long latestDataVersionId;

}
