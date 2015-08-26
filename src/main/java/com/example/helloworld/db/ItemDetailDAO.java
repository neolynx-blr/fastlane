package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.example.helloworld.core.ItemDetail;
import com.google.common.base.Optional;

/**
 * Created by nitesh.garg on 25-Aug-2015
 */
public class ItemDetailDAO extends AbstractDAO<ItemDetail> {
	public ItemDetailDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<ItemDetail> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public ItemDetail create(ItemDetail itemDetail) {
		return persist(itemDetail);
	}

	public List<ItemDetail> findAll() {
		return list(namedQuery("com.example.helloworld.core.ItemDetail.findAll"));
	}

}
