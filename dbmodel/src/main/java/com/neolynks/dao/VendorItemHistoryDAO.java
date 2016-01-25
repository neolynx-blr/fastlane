package com.neolynks.dao;

import com.google.common.base.Optional;
import com.neolynks.model.VendorItemHistory;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

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
		return list(namedQuery("com.neolynks.model.VendorItemHistory.findByVendor").setParameter("vendorId",
				vendorId));
	}

}
