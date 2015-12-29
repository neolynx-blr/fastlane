package com.neolynks.curator.manager;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynks.curator.meta.CartLogistics;
import com.neolynks.curator.model.Cart;

/**
 * Created by nitesh.garg on Dec 29, 2015
 *
 */

public class CartOperator implements Runnable {

	static Logger LOGGER = LoggerFactory.getLogger(CartOperator.class);
	
	final LoadingCache<String, Cart> cartCache;
	
	/**
	 * @param cartCache
	 */
	public CartOperator(LoadingCache<String, Cart> cartCache) {
		super();
		this.cartCache = cartCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		// Keeping running for ever in the background looking for new inventory
		while (true) {

			try {

				// Check for new inventory every 'X' seconds.
				Thread.sleep(10000);

				if (CartLogistics.getInstance().getUpdatedCartIds().size() > 0) {
					LOGGER.debug("Need to process [{}] unsynced carts...", CartLogistics.getInstance()
							.getUpdatedCartIds().size());

					// TODO ensure parallel addition/won't hurt
					for (String cartId : CartLogistics.getInstance().getUpdatedCartIds()) {
						
						int count = 0;
						Cart unsyncedCart = this.cartCache.get(cartId);
						Set<Long> barcodeKeys = unsyncedCart.getItemList().keySet();

						for(Long barcode : barcodeKeys) {
							if(! unsyncedCart.getAdminSyncedBarcodeCount().containsKey(barcode)) {
								
								count++;
								// TODO Shukla!!! Remember to check for status & items check
								unsyncedCart.getAdminSyncedBarcodeCount().put(barcode, unsyncedCart.getItemList().get(barcode).getCountForInStorePickup() + unsyncedCart.getItemList().get(barcode).getCountForDelivery());
								
							}
						}
						
						unsyncedCart.setCartSyncedWithAdmin(Boolean.TRUE);
						
						this.cartCache.put(cartId, unsyncedCart);
						LOGGER.debug("Completed updating [{}] items of cart [{}] to the logistics service", count, cartId);

					}

					LOGGER.debug("Completed. New set of synced and updated carts are [{}] and [{}]", CartLogistics
							.getInstance().getSyncedCartIds().size(), CartLogistics.getInstance().getUpdatedCartIds()
							.size());

				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// Eat away, ensuring infinite loop continues for cart operations
			}

		}

	}

}
