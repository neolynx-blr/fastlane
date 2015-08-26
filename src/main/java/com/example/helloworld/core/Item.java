package com.example.helloworld.core;

import lombok.Data;

/**
 * Created by nitesh.garg on 24-Aug-2015
 *
 */

@Data
public class Item {

    /**
     * Pricing view can make things tricky here,
     * 1. Vendor is selected, has no automated system hence no available pricing
     * 2. Vendor is selected, has pricing available, core use-case, all is well
     * 3. Vendor is not selected and search is working on product hence the pricing will only be indicative. We may
     * not show prices at all, or show lowest price of that item across vendors and indicate the same to the user
     *
     * Currently we are not solving for #3,
     * Even for #1, the pricing part of this class can be broken away as that would be applicable only at times
     */

    // Applicable when the vendor is selected
    private Long itemId;

    // TODO: Need to consider if this can be dropped always assuming the latest version only
    private Long itemVersionId;

    // Assuming no vendor is selected, items will be picked dynamically later on pricing and discount at item level ?
    private Long productId;

    /**
     * 1. If vendor is selected, show relatively accurate pricing
     * 2. Else if delivery address is known (by default or picked just now), show best prices based on nearby vendors
     * 3. Else, ? a. Don't show prices or b. Show indicative prices and ask to pick either #1 or #2 for accuracy
     *
     * This ties-up with the comment above
     */
    private Float mrp;
    private Float price;

    //private DiscountType discountType;
    private Float discountValue;

    /**
     * Following items are applicable whether search was item based (specific to Vendor(s)) or product based.
     * 1. This collects all the information we'll use for displaying while search and first level detail
     * 2. Image URLs for sizes shown during search and then display
     */
    private String searchImageURL;
    private String displayImageURL;
    private String productAttributeJSON;

}
