package com.neolynx.curator.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.google.common.base.Optional;
import com.neolynx.curator.core.VendorItemHistory;

/**
 * Created by nitesh.garg on 05-Sep-2015
 */
public class VendorItemHistoryDAO extends AbstractDAO<VendorItemHistory> {
	public VendorItemHistoryDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<VendorItemHistory> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public VendorItemHistory create(VendorItemHistory itemDetail) {
		return persist(itemDetail);
	}

	public List<VendorItemHistory> findByVendor(Long vendorId) {
		return list(namedQuery("com.example.helloworld.core.VendorItemHistory.findByVendor").setParameter("vendorId",
				vendorId));
	}

}
