package com.neolynks.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by nitesh.garg on 04-Sep-2015
 */

@Data
public class ProductVendorMap implements Serializable {

	private static final long serialVersionUID = 1L;

	@Setter(AccessLevel.NONE)
	private List<Long> vendorIds;
	ProductMaster productMaster;

	public ProductVendorMap(ProductMaster productMaster) {
		super();
		this.productMaster = productMaster;
		setVendorIds();
	}

	public List<Long> getVendorIds() {
		if (this.vendorIds == null || this.vendorIds.size() < 1) {
			setVendorIds();
		}
		return this.vendorIds;
	}

	private void setVendorIds() {

		if (this.productMaster != null) {
			String vendorId = this.productMaster.getVendorId();
			StringTokenizer st = new StringTokenizer(vendorId, ",");
			vendorIds = new ArrayList<Long>();
			while (st.hasMoreElements()) {
				vendorIds.add(Long.parseLong(st.nextToken()));
			}
		}
	}
	
	public void removeVendor(Long vendorId) {
		this.vendorIds.remove(vendorId);
	}
	
	public String getVendorsAsStringList() {
		return StringUtils.join(this.vendorIds, ",");
	}

}
