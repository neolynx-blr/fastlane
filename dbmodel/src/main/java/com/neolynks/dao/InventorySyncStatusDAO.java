package com.neolynks.dao;

import com.google.common.base.Optional;
import com.neolynks.model.InventorySyncStatus;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

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
		return list(namedQuery("com.neolynks.model.findAll"));
	}

}
