package com.neolynks.api.common.inventory;

import com.neolynks.api.userapp.price.ItemPrice;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Created by nitesh.garg on Oct 28, 2015
 *
s */
@Slf4j
@Data
public class ItemInfo implements Serializable {

	private static final long serialVersionUID = 594832882355959992L;

    private String itemCode;
	private ItemPrice itemPrice;
	private ProductInfo productInfo;

}
