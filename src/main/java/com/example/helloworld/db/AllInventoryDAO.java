package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.example.helloworld.core.AllInventory;
import com.google.common.base.Optional;

/**
 * Created by nitesh.garg on 27-Aug-2015
 */
public class AllInventoryDAO extends AbstractDAO<AllInventory> {
	public AllInventoryDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<AllInventory> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public AllInventory create(AllInventory allInventory) {
		return persist(allInventory);
	}

	public List<AllInventory> findAll() {
		return list(namedQuery("com.example.helloworld.core.AllInventory.findAll"));
	}

}
