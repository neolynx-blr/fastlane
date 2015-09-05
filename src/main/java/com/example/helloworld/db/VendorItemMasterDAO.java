package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.example.helloworld.core.VendorItemMaster;
import com.google.common.base.Optional;

/**
 * Created by nitesh.garg on 25-Aug-2015
 */
public class VendorItemMasterDAO extends AbstractDAO<VendorItemMaster> {
	public VendorItemMasterDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<VendorItemMaster> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public VendorItemMaster create(VendorItemMaster vendorItemMaster) {
		return persist(vendorItemMaster);
	}

	public List<VendorItemMaster> findByVendor(Long vendorId) {
		return list(namedQuery("com.example.helloworld.core.VendorItemMaster.findByVendor").setParameter("vendorId",
				vendorId));
	}
}
