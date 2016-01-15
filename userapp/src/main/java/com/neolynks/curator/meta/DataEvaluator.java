package com.neolynks.curator.meta;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynks.curator.manager.CartHandler;

/**
 * Created by nitesh.garg on Dec 27, 2015
 *
 */

// TODO Need to build the entity objects sincerely here
public class DataEvaluator {

	private static LoadingCache<Long, Long> vendorVersionCache;

	static Logger LOGGER = LoggerFactory.getLogger(CartHandler.class);

	/**
	 * @param vendorVersionCache
	 */
	public DataEvaluator(LoadingCache<Long, Long> vendorVersionCacheRef) {
		super();
		vendorVersionCache = vendorVersionCacheRef;
	}

	public static VendorInfo getVendorDetails(Long vendorId) {

		VendorInfo vendorMeta = new VendorInfo();
		vendorMeta.setVendorId(vendorId);

		try {
			vendorMeta.setLatestDataVersionId(vendorVersionCache.get(vendorId));
		} catch (ExecutionException e) {
			LOGGER.error("Error [{}] occurred while reading latest data-version-id for vendor [{}]", e.getMessage(),
					vendorId);
			e.printStackTrace();
		}

		return vendorMeta;

	}

	public static UserInfo getUserDetails(String userId) {

		UserInfo userInfo = new UserInfo();
		userInfo.setUserId(userId);
		return userInfo;

	}
}
