package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.example.helloworld.core.VendorVersionDifferential;
import com.google.common.base.Optional;

/**
 * Created by nitesh.garg on 05-Sep-2015
 */
public class VendorVersionDifferentialDAO extends AbstractDAO<VendorVersionDifferential> {
	public VendorVersionDifferentialDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<VendorVersionDifferential> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public VendorVersionDifferential create(VendorVersionDifferential vendorVersionDifferential) {
		return persist(vendorVersionDifferential);
	}

	public VendorVersionDifferential findByVendorVersion(Long vendorId, Long versionId) {
		return list(
				namedQuery("com.example.helloworld.core.VendorVersionDifferential.findByVendorVersion").setParameter(
						"vendorId", vendorId).setParameter("versionId", versionId)).get(0);
	}

	public List<VendorVersionDifferential> findAll() {
		return list(namedQuery("com.example.helloworld.core.VendorVersionDifferential.findAll"));
	}
}
