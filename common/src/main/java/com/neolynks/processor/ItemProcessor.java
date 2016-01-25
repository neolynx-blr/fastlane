package com.neolynks.processor;


import com.neolynks.api.common.inventory.ItemInfo;
import com.neolynks.api.userapp.price.DiscountBase;
import com.neolynks.api.userapp.price.DiscountDetail;
import com.neolynks.api.userapp.price.DiscountInfo;
import com.neolynks.api.userapp.price.DiscountPlayer;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by nitesh.garg on Nov 29, 2015
 *
 */

@Data
public class ItemProcessor {

	/**
	 * MRP is arbitrary number with no real implications other than sometimes being considered for discounts
	 * 
	 * fn(basePrice, tax%) = taxAmount
	 * 
	 * selling-price = basePrice + taxAmount - individualItemDiscountAmount
	 * netPrice = fn ( (count * price), cummulative-discount-if-applicable)
	 * 
	 */	

    //TODO: Understand this piece here
    public static Double calculatePricing(ItemInfo itemInfo, int itemCount) {
        Double netPrice = 0.0D;
        Double sellingPrice = itemInfo.getItemPrice().getSellingPrice();
        DiscountDetail discountDetail = itemInfo.getItemPrice().getDiscountDetail();

        Double maxDiscount = 0.0D;
        Date currentDate = Calendar.getInstance().getTime();
        Double priceBeforeDiscount = itemCount * sellingPrice;

        if (discountDetail != null && CollectionUtils.isNotEmpty(discountDetail.getDiscountInfo())) {
            for (DiscountInfo discountInfo : discountDetail.getDiscountInfo()) {
                if (currentDate.after(discountInfo.getStartDate()) && currentDate.before(discountInfo.getEndDate())) {
                    if (discountInfo.getDiscountOn().getPlayer() == DiscountPlayer.SELF) {
                        for (DiscountBase discountFor : discountInfo.getDiscountFor()) {
                            if (discountFor.getPlayer() == DiscountPlayer.SELF) {
                                maxDiscount += 1.0D;
                                priceBeforeDiscount += 1.0D;
                            }
                        }
                    }
                }
            }
        }
        return netPrice;
    }

}
