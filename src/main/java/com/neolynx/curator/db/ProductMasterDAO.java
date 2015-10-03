package com.neolynx.curator.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.SessionFactory;

import com.google.common.base.Optional;
import com.neolynx.curator.core.ProductMaster;

/**
 * Created by nitesh.garg on 25-Aug-2015
 */
public class ProductMasterDAO extends AbstractDAO<ProductMaster> {
	public ProductMasterDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<ProductMaster> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public ProductMaster create(ProductMaster productCore) {
		return persist(productCore);
	}

	public List<ProductMaster> findAll() {
		return list(namedQuery("com.example.helloworld.core.ProductMaster.findAll"));
	}

	public ProductMaster findByBarcode(Long barcode) {
		return list(
				namedQuery("com.example.helloworld.core.ProductMaster.fetchByBarcode").setParameter("barcode", barcode))
				.get(0);
	}

	public List<ProductMaster> findByVendor(Long vendorId) {
		return list(namedQuery("com.example.helloworld.core.ProductMaster.findByVendor").setParameter(
				"vendorIdPattern", "%" + String.valueOf(vendorId) + "%"));
	}

	public List<ProductMaster> findExclusiveToVendor(Long vendorId) {
		return list(namedQuery("com.example.helloworld.core.ProductMaster.findExclusiveToVendor").setParameter(
				"vendorId", String.valueOf(vendorId)));
	}

	public void deleteExclusiveToVendor(Long vendorId) {
		List<ProductMaster> productMasterByVendorDetails = findExclusiveToVendor(vendorId);
		if (CollectionUtils.isNotEmpty(productMasterByVendorDetails)) {
			for (ProductMaster instance : productMasterByVendorDetails) {
				this.currentSession().delete(instance);
			}
		}
	}

}
