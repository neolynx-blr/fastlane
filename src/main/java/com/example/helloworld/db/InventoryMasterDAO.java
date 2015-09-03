package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.example.helloworld.core.InventoryMaster;
import com.google.common.base.Optional;

/**
 * Created by nitesh.garg on 27-Aug-2015
 */
public class InventoryMasterDAO extends AbstractDAO<InventoryMaster> {
	public InventoryMasterDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<InventoryMaster> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public InventoryMaster create(InventoryMaster allInventory) {
		return persist(allInventory);
	}

	public List<InventoryMaster> getLatestInventoryByVendor(Long vendorId) {
		return list(namedQuery(
				"com.example.helloworld.core.InventoryMaster.findLatestInventoryByVendor")
				.setParameter("vendorId", vendorId));
	}

	public List<InventoryMaster> getRecentInventoryUpdatesByVendor(Long vendorId, Long lastSyncedVersionId) {
		return list(namedQuery(
				"com.example.helloworld.core.InventoryMaster.findRecentInventoryUpdates")
				.setParameter("vendorId", vendorId).setParameter("lastSyncedVersionId", lastSyncedVersionId));
	}
}
