package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.example.helloworld.core.InventorySync;
import com.google.common.base.Optional;

/**
 * Created by nitesh.garg on 29-Aug-2015
 */
public class InventorySyncDAO extends
		AbstractDAO<InventorySync> {
	public InventorySyncDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<InventorySync> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public InventorySync create(
			InventorySync inventorySync) {
		return persist(inventorySync);
	}

	public List<InventorySync> findAll() {
		return list(namedQuery("com.example.helloworld.core.InventorySync.findAll"));
	}

}
