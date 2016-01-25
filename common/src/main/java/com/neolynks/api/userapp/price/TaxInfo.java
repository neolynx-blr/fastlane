package com.neolynks.api.userapp.price;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by nitesh.garg on Oct 28, 2015
 *
 */
@Data
@AllArgsConstructor
public class TaxInfo implements Serializable {

	private static final long serialVersionUID = -4773014927161242633L;

	private final Integer taxType;
	private final Double taxValue;
}
