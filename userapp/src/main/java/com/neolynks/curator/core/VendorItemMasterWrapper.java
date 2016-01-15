package com.neolynks.curator.core;

import java.io.IOException;

import com.neolynks.api.common.inventory.ItemInfo;
import com.neolynks.api.common.inventory.ProductInfo;
import com.neolynks.api.userapp.price.DiscountDetail;
import com.neolynks.api.userapp.price.ItemPrice;
import com.neolynks.api.userapp.price.TaxDetail;
import lombok.Data;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by nitesh.garg on Oct 30, 2015
 *
 */

@Data
public class VendorItemMasterWrapper {

	public VendorItemMasterWrapper(VendorItemMaster vimRecord) {
		this.setVimRecord(vimRecord);
	}

	public VendorItemMasterWrapper(VendorItemHistory vihRecord) {
		this.setVihRecord(vihRecord);
	}

	VendorItemMaster vimRecord;
	VendorItemHistory vihRecord;

	static Logger LOGGER = LoggerFactory.getLogger(VendorItemMasterWrapper.class);

	public ItemInfo generateVIMItemInfo() {

		ItemInfo returnItemInfo = new ItemInfo();
        returnItemInfo.setItemCode(this.getVimRecord().getItemCode());

		ItemPrice itemPrice = new ItemPrice();

		itemPrice.setMrp(this.getVimRecord().getMrp());
		itemPrice.setBasePrice(this.getVimRecord().getBasePrice());
		itemPrice.setSellingPrice(this.getVimRecord().getSellingPrice());

		try {
			ObjectMapper mapper = new ObjectMapper();

			itemPrice.setTaxDetail(StringUtils.isNotEmpty(this.getVimRecord().getTaxJSON()) ? mapper.readValue(this
					.getVimRecord().getTaxJSON(), TaxDetail.class) : null);

			itemPrice.setDiscountDetail(StringUtils.isNotEmpty(this.getVimRecord().getDiscountJSON()) ? mapper
					.readValue(this.getVimRecord().getDiscountJSON(), DiscountDetail.class) : null);

		} catch (IOException e) {
			LOGGER.error("Received error [{}] while deserializins the tax or discount JSONs [{}], [{}]", this
					.getVimRecord().getTaxJSON(), this.getVimRecord().getDiscountJSON(), e.getMessage());
		}

		ProductInfo productInfo = new ProductInfo();
		productInfo.setName(this.getVimRecord().getName());
		productInfo.setBrandName(this.getVimRecord().getBrandName());
		productInfo.setTagLine(this.getVimRecord().getTagLine());
		productInfo.setImageJSON(this.getVimRecord().getImageJSON());
        productInfo.setBarcode(this.getVimRecord().getBarcode());
		returnItemInfo.setItemPrice(itemPrice);
		returnItemInfo.setProductInfo(productInfo);

		return returnItemInfo;

	}
	
	public ItemInfo generateVIHItemInfo() {

		ItemInfo returnItemInfo = new ItemInfo();
		returnItemInfo.setItemCode(this.getVihRecord().getItemCode());

        ItemPrice itemPrice = new ItemPrice();

        itemPrice.setMrp(this.getVihRecord().getMrp());
        itemPrice.setSellingPrice(this.getVihRecord().getSellingPrice());
        itemPrice.setBasePrice(this.getVihRecord().getBasePrice());

        try {
            ObjectMapper mapper = new ObjectMapper();

            itemPrice.setTaxDetail(StringUtils.isNotEmpty(this.getVihRecord().getTaxJSON()) ? mapper.readValue(
                    this.getVihRecord().getTaxJSON(), TaxDetail.class) : null);

            itemPrice.setDiscountDetail(StringUtils.isNotEmpty(this.getVihRecord().getDiscountJSON()) ? mapper.readValue(
                    this.getVihRecord().getDiscountJSON(), DiscountDetail.class) : null);

        } catch (IOException e) {
            LOGGER.error("Received error [{}] while deserializins the tax or discount JSONs [{}], [{}]",
                    this.getVihRecord().getTaxJSON(), this.getVihRecord().getDiscountJSON(), e.getMessage());
        }

        ProductInfo productInfo = new ProductInfo();
        productInfo.setBarcode(this.getVihRecord().getBarcode());
        productInfo.setName(this.getVihRecord().getName());
        productInfo.setTagLine(this.getVihRecord().getTagLine());
        productInfo.setImageJSON(this.getVihRecord().getImageJSON());

        returnItemInfo.setItemPrice(itemPrice);
        returnItemInfo.setProductInfo(productInfo);

        return returnItemInfo;
    }

	public ItemInfo generateDifferential(VendorItemMaster oldItem, VendorItemMaster newItem) {

		VendorItemMasterWrapper oldVIMWrapper = new VendorItemMasterWrapper(oldItem);
		VendorItemMasterWrapper newVIMWrapper = new VendorItemMasterWrapper(newItem);

		ItemInfo deltaItemInfo = oldVIMWrapper.generateVIMItemInfo();
		ItemInfo newItemInfo = newVIMWrapper.generateVIMItemInfo();
		deltaItemInfo.updateThisWithLatestItemData(newItemInfo);

		return deltaItemInfo;

	}

}
