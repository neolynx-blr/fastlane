package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.example.helloworld.core.ProductMaster;
import com.google.common.base.Optional;

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

	public ProductMaster findByBarcodeId(Long barcode) {
		return list(
				namedQuery(
						"com.example.helloworld.core.ProductMaster.fetchByBarcode")
						.setParameter("barcode", barcode)).get(0);
	}
}
