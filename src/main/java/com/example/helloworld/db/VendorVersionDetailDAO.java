package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.example.helloworld.core.VendorVersionDetail;
import com.google.common.base.Optional;

/**
 * Created by nitesh.garg on 05-Sep-2015
 */
public class VendorVersionDetailDAO extends AbstractDAO<VendorVersionDetail> {
	public VendorVersionDetailDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<VendorVersionDetail> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public VendorVersionDetail create(VendorVersionDetail vendorVersionDetail) {
		return persist(vendorVersionDetail);
	}

	public List<VendorVersionDetail> findByVendor(Long vendorId) {
		return list(namedQuery("com.example.helloworld.core.VendorVersionDetail.findByVendor").setParameter("vendorId",
				vendorId));
	}

}
