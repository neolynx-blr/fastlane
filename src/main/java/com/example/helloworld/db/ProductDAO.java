package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.example.helloworld.core.Product;
import com.google.common.base.Optional;

public class ProductDAO extends AbstractDAO<Product> {
	public ProductDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<Product> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public Product create(Product product) {
		return persist(product);
	}

	public List<Product> findAll() {
		return list(namedQuery("com.example.helloworld.core.Product.findAll"));
	}
}
