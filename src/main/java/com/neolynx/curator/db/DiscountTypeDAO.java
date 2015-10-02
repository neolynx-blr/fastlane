package com.neolynx.curator.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.google.common.base.Optional;
import com.neolynx.curator.core.DiscountType;

/**
 * Created by nitesh.garg on 25-Aug-2015
 */

public class DiscountTypeDAO extends AbstractDAO<DiscountType> {
	public DiscountTypeDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<DiscountType> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public DiscountType create(DiscountType discountType) {
		return persist(discountType);
	}

	public List<DiscountType> findAll() {
		return list(namedQuery("com.example.helloworld.core.DiscountType.findAll"));
	}

}
