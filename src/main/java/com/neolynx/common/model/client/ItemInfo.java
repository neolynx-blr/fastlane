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

	public ItemInfo() {
		super();
	}

}
