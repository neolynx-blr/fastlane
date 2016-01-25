package com.neolynks.api.userapp.price;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nitesh.garg on Oct 28, 2015
 *
 */

@Data
public class TaxDetail implements Serializable {

	private static final long serialVersionUID = -1224105408199308790L;

	private List<TaxInfo> taxInfo = new ArrayList<TaxInfo>();
}
