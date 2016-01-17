package com.neolynks.dao;

import com.google.common.base.Optional;
import com.neolynks.model.Vendor;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

/**
 * Created by nitesh.garg on 25-Aug-2015
 */
public class VendorDAO extends AbstractDAO<Vendor> {
	public VendorDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<Vendor> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public Vendor create(Vendor vendor) {
		return persist(vendor);
	}

	public List<Vendor> findAll() {
		return list(namedQuery("com.example.helloworld.core.Vendor.findAll"));
	}
}
