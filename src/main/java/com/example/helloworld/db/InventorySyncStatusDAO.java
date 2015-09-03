package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.example.helloworld.core.InventorySyncStatus;
import com.google.common.base.Optional;

/**
 * Created by nitesh.garg on 29-Aug-2015
 */
public class InventorySyncStatusDAO extends
		AbstractDAO<InventorySyncStatus> {
	public InventorySyncStatusDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<InventorySyncStatus> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public InventorySyncStatus create(
			InventorySyncStatus inventorySync) {
		return persist(inventorySync);
	}

	public List<InventorySyncStatus> findAll() {
		return list(namedQuery("com.example.helloworld.core.InventorySyncStatus.findAll"));
	}

}
