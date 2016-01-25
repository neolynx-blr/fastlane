package com.neolynks.api.userapp;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by nitesh.garg on Oct 22, 2015
 *
 */

@Data
public class UserDetail implements Serializable {

	private static final long serialVersionUID = -8197374004008782447L;
	
	private String userId;

	private Map<String, String> deviceIdMap;
}
