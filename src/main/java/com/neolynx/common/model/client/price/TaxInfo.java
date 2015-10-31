package com.neolynx.common.model.client.price;

import lombok.Data;

/**
 * Created by nitesh.garg on Oct 28, 2015
 *
 */

@Data
public class TaxInfo {

	private Integer taxType;
	private Double taxValue;

	public Boolean isDifferent(TaxInfo taxInfo) {

		if (taxInfo == null) {
			return Boolean.TRUE;
		}

		if ((this.taxType == null && taxInfo.getTaxType() != null)
				|| (this.taxType.compareTo(taxInfo.getTaxType()) != 0)) {
			return Boolean.TRUE;
		}

		if ((this.taxValue == null && taxInfo.getTaxValue() != null)
				|| (this.taxValue.compareTo(taxInfo.getTaxValue()) != 0)) {
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

}
