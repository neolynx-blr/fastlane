package com.neolynks.dao;

import com.google.common.base.Optional;
import com.neolynks.model.VendorItemMaster;
import io.dropwizard.hibernate.AbstractDAO;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.SessionFactory;

import java.util.List;

/**
 * Created by nitesh.garg on 25-Aug-2015
 */
public class VendorItemMasterDAO extends AbstractDAO<VendorItemMaster> {
	public VendorItemMasterDAO(SessionFactory factory) {
		super(factory);
	}

	public Optional<VendorItemMaster> findById(Long id) {
		return Optional.fromNullable(get(id));
	}

	public VendorItemMaster create(VendorItemMaster vendorItemMaster) {
		return persist(vendorItemMaster);
	}

	public List<VendorItemMaster> findByVendor(Long vendorId) {
		return list(namedQuery("com.example.helloworld.core.VendorItemMaster.findByVendor").setParameter("vendorId",
				vendorId));
	}
	
	public void deleteByVendorId(Long vendorId) {
		List<VendorItemMaster> vendorItemRecords = findByVendor(vendorId);
		if (CollectionUtils.isNotEmpty(vendorItemRecords)) {
			for(VendorItemMaster instance : vendorItemRecords) {
				this.currentSession().delete(instance);				
			}
		}
	}

}
