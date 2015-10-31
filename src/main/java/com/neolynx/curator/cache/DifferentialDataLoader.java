package com.neolynx.curator.cache;

import io.dropwizard.hibernate.UnitOfWork;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheLoader;
import com.neolynx.common.model.client.InventoryInfo;
import com.neolynx.curator.core.VendorVersionDifferential;
import com.neolynx.curator.util.Constants;

/**
 * Created by nitesh.garg on 06-Sep-2015
 */
public class DifferentialDataLoader extends CacheLoader<String, InventoryInfo> {

	private SessionFactory sessionFactory;
	static Logger LOGGER = LoggerFactory.getLogger(DifferentialDataLoader.class);

	public DifferentialDataLoader(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.common.cache.CacheLoader#load(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@UnitOfWork
	@Override
	public InventoryInfo load(String vendorVersionKey) throws Exception {

		if (vendorVersionKey == null) {
			LOGGER.debug("Tried loading differential data for NULL vendor-version-id combination, obviously failed.");
			return null;
		}

		InventoryInfo inventoryInfo = new InventoryInfo();
		List<String> vendorVersion = new StrTokenizer(vendorVersionKey, Constants.CACHE_KEY_SEPARATOR_STRING)
				.getTokenList();

		Long vendorId = Long.parseLong(vendorVersion.get(0));
		Long versionId = Long.parseLong(vendorVersion.get(1));

		LOGGER.debug("Looking to load differential cache for vendor-version [{}-{}]", vendorId, versionId);

		Session session = sessionFactory.openSession();

		// Check the DB data for this vendor-version combination
		Query diffQuery = session
				.createSQLQuery(
						" select vvd.* from vendor_version_differential vvd where vendor_id = :vendorId and version_id = :versionId and last_synced_version_id != 0 and is_valid = 't' and is_this_latest_version != 't' ")
				.addEntity("vendor_version_differential", VendorVersionDifferential.class)
				.setParameter("vendorId", vendorId).setParameter("versionId", versionId);

		List<VendorVersionDifferential> vendorVersionDifferentials = diffQuery.list();

		if (CollectionUtils.isEmpty(vendorVersionDifferentials)) {
			LOGGER.debug("Unable to find DB entry for Vendor-Version [{}-{}]", vendorId, versionId);
		} else {

			VendorVersionDifferential diffInstance = vendorVersionDifferentials.get(0);

			ObjectMapper mapper = new ObjectMapper();
			try {
				inventoryInfo = mapper.readValue(diffInstance.getDifferentialData(), InventoryInfo.class);
			} catch (IOException e) {
				LOGGER.error(
						"Received error [{}] while deserializing the InventoryInfo from the DB while building differential cache for vendor [{}], version [{}]",
						e.getMessage(), vendorId, versionId);
			}
		}

		session.close();

		return inventoryInfo;
	}

}
