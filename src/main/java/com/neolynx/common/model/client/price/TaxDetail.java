package com.neolynx.common.model.client.price;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Created by nitesh.garg on Oct 28, 2015
 *
 */

@Data
public class TaxDetail {

	private List<TaxInfo> taxInfo = new ArrayList<TaxInfo>();

	public Boolean isDifferent(TaxDetail newTaxDetail) {

		for (TaxInfo outerInstance : this.getTaxInfo()) {
			for (TaxInfo innerInstance : newTaxDetail.getTaxInfo()) {
				if (outerInstance.isDifferent(innerInstance)) {
					return Boolean.TRUE;
				}
			}
		}

		return Boolean.FALSE;

	}

}
