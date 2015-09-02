package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.example.helloworld.core.ProductCore;
import com.google.common.base.Optional;

/**
 * Created by nitesh.garg on 25-Aug-2015
 */
public class ProductCoreDAO extends AbstractDAO<ProductCore> {
	public ProductCoreDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<ProductCore> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public ProductCore create(ProductCore productCore) {
		return persist(productCore);
	}

	public List<ProductCore> findAll() {
		return list(namedQuery("com.example.helloworld.core.ProductCore.findAll"));
	}

	public ProductCore findByBarcodeId(Long barcode) {
		return list(
				namedQuery(
						"com.example.helloworld.core.ProductCore.fetchByBarcode")
						.setParameter("barcode", barcode)).get(0);
	}
}
