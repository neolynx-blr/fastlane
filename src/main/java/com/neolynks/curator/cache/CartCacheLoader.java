package com.neolynks.curator.cache;

import io.dropwizard.hibernate.UnitOfWork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheLoader;
import com.neolynks.curator.model.Cart;

/**
 * Created by nitesh.garg on 26-Dec-2015
 * 
 */
public class CartCacheLoader extends CacheLoader<String, Cart> {

	static Logger LOGGER = LoggerFactory.getLogger(CartCacheLoader.class);

	@UnitOfWork
	@Override
	public Cart load(String cartId) throws Exception {

		Cart cart = null;
		if (cartId == null) {
			LOGGER.debug("Tried loading latest version of NULL vendor-id, obviously failed.");
		} else {
		}
		return cart;
	}

}
