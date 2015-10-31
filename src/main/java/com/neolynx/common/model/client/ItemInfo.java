/**
 * 
 */
package com.neolynx.common.model.client;

import java.io.IOException;
import java.io.Serializable;

import lombok.Data;

import org.jadira.usertype.spi.utils.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neolynx.common.model.client.price.DiscountDetail;
import com.neolynx.common.model.client.price.ItemPrice;
import com.neolynx.common.model.client.price.TaxDetail;
import com.neolynx.curator.core.VendorItemHistory;
import com.neolynx.curator.core.VendorItemMaster;

/**
 * Created by nitesh.garg on Oct 28, 2015
 *
 */

@Data
public class ItemInfo implements Serializable {

	static Logger LOGGER = LoggerFactory.getLogger(ItemInfo.class);
	private static final long serialVersionUID = 594832882355959992L;

	private String barcode;
	private String itemCode;

	private ItemPrice itemPrice;
	private ProductInfo productInfo;

	/**
	 * TODO Throw error if the item code or barcode doesn't match
	 */

	public ItemInfo generateDifferentialFrom(ItemInfo latestItemData) {

		ItemInfo deltaItemInfo = new ItemInfo();

		deltaItemInfo.setBarcode(this.getBarcode());
		deltaItemInfo.setItemCode(this.getItemCode());

		if (this.getItemPrice() == null || latestItemData.getItemPrice() == null) {
			deltaItemInfo.setItemPrice(latestItemData.getItemPrice());
		} else {
			deltaItemInfo.setItemPrice(this.getItemPrice().generateDifferentialFrom(latestItemData.getItemPrice()));
		}

		if (this.getProductInfo() == null) {
			deltaItemInfo.setProductInfo(latestItemData.getProductInfo());
		} else if (latestItemData.getProductInfo() == null) {
			deltaItemInfo.setProductInfo(this.getProductInfo());
		} else {
			deltaItemInfo.setProductInfo(this.getProductInfo()
					.generateDifferentialFrom(latestItemData.getProductInfo()));
		}

		return deltaItemInfo;

	}

	public void mergeOnlyNewUpdatesFrom(ItemInfo updateFromLatestItemData) {

		if (updateFromLatestItemData.getProductInfo().getIsName()) {
			this.getProductInfo().setName(updateFromLatestItemData.getProductInfo().getName());
		}

		if (updateFromLatestItemData.getProductInfo().getIsDescription()) {
			this.getProductInfo().setDescription(updateFromLatestItemData.getProductInfo().getDescription());
		}

		if (updateFromLatestItemData.getProductInfo().getIsImageJSON()) {
			this.getProductInfo().setImageJSON(updateFromLatestItemData.getProductInfo().getImageJSON());
		}

		if (updateFromLatestItemData.getProductInfo().getIsTagLine()) {
			this.getProductInfo().setTagLine(updateFromLatestItemData.getProductInfo().getTagLine());
		}
	}

	public void updateThisWithLatestItemData(VendorItemMaster latestItemData) {

		this.getItemPrice().setMrp(latestItemData.getMrp());
		this.getItemPrice().setPrice(latestItemData.getPrice());
		this.getItemPrice().setBasePrice(latestItemData.getBasePrice());

		try {
			ObjectMapper mapper = new ObjectMapper();

			this.getItemPrice().setTaxDetail(
					StringUtils.isNotEmpty(latestItemData.getTaxJSON()) ? mapper.readValue(latestItemData.getTaxJSON(),
							TaxDetail.class) : null);

			this.getItemPrice().setDiscountDetail(
					StringUtils.isNotEmpty(latestItemData.getDiscountJSON()) ? mapper.readValue(
							latestItemData.getDiscountJSON(), DiscountDetail.class) : null);

		} catch (IOException e) {
			LOGGER.error("Received error [{}] while deserializins the tax or discount JSONs [{}], [{}]",
					latestItemData.getTaxJSON(), latestItemData.getDiscountJSON(), e.getMessage());
		}

		this.getProductInfo().reset();

		this.getProductInfo().setName(latestItemData.getName());
		this.getProductInfo().setTagLine(latestItemData.getTagLine());
		this.getProductInfo().setImageJSON(latestItemData.getImageJSON());
		this.getProductInfo().setDescription(latestItemData.getDescription());
	}

	public ItemInfo(VendorItemMaster vendorItemMaster) {

		this.setBarcode(vendorItemMaster.getBarcode());
		this.setItemCode(vendorItemMaster.getItemCode());

		ItemPrice itemPrice = new ItemPrice();

		itemPrice.setMrp(vendorItemMaster.getMrp());
		itemPrice.setPrice(vendorItemMaster.getPrice());
		itemPrice.setBasePrice(vendorItemMaster.getBasePrice());

		try {
			ObjectMapper mapper = new ObjectMapper();

			itemPrice.setTaxDetail(StringUtils.isNotEmpty(vendorItemMaster.getTaxJSON()) ? mapper.readValue(
					vendorItemMaster.getTaxJSON(), TaxDetail.class) : null);

			itemPrice.setDiscountDetail(StringUtils.isNotEmpty(vendorItemMaster.getDiscountJSON()) ? mapper.readValue(
					vendorItemMaster.getDiscountJSON(), DiscountDetail.class) : null);

		} catch (IOException e) {
			LOGGER.error("Received error [{}] while deserializins the tax or discount JSONs [{}], [{}]",
					vendorItemMaster.getTaxJSON(), vendorItemMaster.getDiscountJSON(), e.getMessage());
		}

		ProductInfo productInfo = new ProductInfo();
		productInfo.setName(vendorItemMaster.getName());
		productInfo.setTagLine(vendorItemMaster.getTagLine());
		productInfo.setImageJSON(vendorItemMaster.getImageJSON());
		productInfo.setDescription(vendorItemMaster.getDescription());

		this.setItemPrice(itemPrice);
		this.setProductInfo(productInfo);

	}

	public ItemInfo(VendorItemHistory vendorItemHistory) {

		this.setBarcode(vendorItemHistory.getBarcode());
		this.setItemCode(vendorItemHistory.getItemCode());

		ItemPrice itemPrice = new ItemPrice();

		itemPrice.setMrp(vendorItemHistory.getMrp());
		itemPrice.setPrice(vendorItemHistory.getPrice());
		itemPrice.setBasePrice(vendorItemHistory.getBasePrice());

		try {
			ObjectMapper mapper = new ObjectMapper();

			itemPrice.setTaxDetail(StringUtils.isNotEmpty(vendorItemHistory.getTaxJSON()) ? mapper.readValue(
					vendorItemHistory.getTaxJSON(), TaxDetail.class) : null);

			itemPrice.setDiscountDetail(StringUtils.isNotEmpty(vendorItemHistory.getDiscountJSON()) ? mapper.readValue(
					vendorItemHistory.getDiscountJSON(), DiscountDetail.class) : null);

		} catch (IOException e) {
			LOGGER.error("Received error [{}] while deserializins the tax or discount JSONs [{}], [{}]",
					vendorItemHistory.getTaxJSON(), vendorItemHistory.getDiscountJSON(), e.getMessage());
		}

		ProductInfo productInfo = new ProductInfo();
		productInfo.setName(vendorItemHistory.getName());
		productInfo.setTagLine(vendorItemHistory.getTagLine());
		productInfo.setImageJSON(vendorItemHistory.getImageJSON());
		productInfo.setDescription(vendorItemHistory.getDescription());

		this.setItemPrice(itemPrice);
		this.setProductInfo(productInfo);

	}

	public ItemInfo() {
		super();
	}

}
