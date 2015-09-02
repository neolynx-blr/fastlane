package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.example.helloworld.core.ItemCore;
import com.google.common.base.Optional;

/**
 * Created by nitesh.garg on 25-Aug-2015
 */
public class ItemCoreDAO extends AbstractDAO<ItemCore> {
	public ItemCoreDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<ItemCore> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public ItemCore create(ItemCore itemCore) {
		return persist(itemCore);
	}

	public List<ItemCore> findAll() {
		return list(namedQuery("com.example.helloworld.core.ItemCore.findAll"));
	}

	public ItemCore findByVendorProduct(Long vendorId, Long productId) {
		return list(
				namedQuery(
						"com.example.helloworld.core.ItemCore.findByVendorProduct")
						.setParameter("vendorId", vendorId).setParameter(
								"productId", productId)).get(0);
	}
}
