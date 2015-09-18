package com.neolynx.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Created by nitesh.garg on 26-Aug-2015
 */

@Data
public class ResponseAudit implements Serializable {

	private static final long serialVersionUID = -790793693651803942L;

	Boolean isError = Boolean.TRUE;

	List<Long> successIds;
	Map<Long, Error> failureIdCodeMap;

	public List<Long> getSuccessIds() {
		if (this.successIds == null) {
			this.successIds = new ArrayList<Long>();
		}
		return this.successIds;
	}

	public Map<Long, Error> getFailureIdCodeMap() {
		if (this.failureIdCodeMap == null) {
			this.failureIdCodeMap = new HashMap<Long, Error>();
		}
		return failureIdCodeMap;
	}

}
