package com.neolynks.api.userapp.price;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by nitesh.garg on Oct 28, 2015
 *
 */

@Data
public class DiscountInfo implements Serializable {

	private static final long serialVersionUID = 1810204767291078266L;

	private Date startDate;
	private Date endDate;

	DiscountBase discountOn;
	List<DiscountBase> discountFor;
}
