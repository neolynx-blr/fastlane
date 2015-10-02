package com.neolynx.curator.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.hibernate.SessionFactory;

import com.google.common.base.Optional;
import com.neolynx.curator.core.InventoryMaster;

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

	public List<InventoryMaster> getInventoryByUniqueConstraint(Long vendorId, Long versionId, String itemCode,
			Long barcode) {
		return list(namedQuery("com.example.helloworld.core.InventoryMaster.findInventoryByUniqueConstraint")
				.setParameter("vendorId", vendorId).setParameter("versionId", versionId)
				.setParameter("itemCode", itemCode).setParameter("barcode", barcode));
	}

	public List<InventoryMaster> getRecentInventoryUpdatesByVendor(Long vendorId, Long lastSyncedVersionId) {
		return list(namedQuery("com.example.helloworld.core.InventoryMaster.findRecentInventoryUpdates").setParameter(
				"vendorId", vendorId).setParameter("lastSyncedVersionId", lastSyncedVersionId));
	}
}
