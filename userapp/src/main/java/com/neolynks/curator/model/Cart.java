package com.neolynks.curator.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.neolynks.api.common.CartStatus;
import com.neolynks.api.common.UserVendorContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * Created by nitesh.garg on Dec 27, 2015
 *
 */

@Data
@EqualsAndHashCode(callSuper=false)
public class Cart implements Serializable {

	private static final long serialVersionUID = -5941199729342263523L;

    private String cartId;
	private UserVendorContext userVendorContext;
	private Map<String, Integer> itemCount = new HashMap<String, Integer>();
    private CartStatus status;
	/**
	 * Captures the interactions with Admin service
	 */
	private Boolean cartSyncedWithWorker = Boolean.FALSE;
	private Map<String, Integer> workerSyncedBarcodeCount = new HashMap<String, Integer>();
	
}