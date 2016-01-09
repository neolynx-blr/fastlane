package com.neolynks.curator.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.neolynks.common.model.cart.CartStatus;
import com.neolynks.curator.meta.CartLogistics;
import com.neolynks.curator.model.Cart;
import com.neolynks.worker.manager.WorkerCartHandler;

/**
 * Created by nitesh.garg on Dec 29, 2015
 *
 */

public class CartOperator implements Runnable {

	static Logger LOGGER = LoggerFactory.getLogger(CartOperator.class);
	
	private final LoadingCache<Long, Cart> cartCache;
	private final WorkerCartHandler workerCartHandler;
	
	/**
	 * @param cartCache
	 */
	public CartOperator(LoadingCache<Long, Cart> cartCache, WorkerCartHandler workerCartHandler) {
		super();
		this.cartCache = cartCache;
		this.workerCartHandler = workerCartHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		// Keeping running for ever in the background looking for new cart-cache updates
		while (true) {

			try {

				// Check for new inventory every 'X' seconds.
				Thread.sleep(10000);

				if (CartLogistics.getInstance().getUpdatedCartIds().size() > 0) {
					LOGGER.debug("Need to process [{}] unsynced carts...", CartLogistics.getInstance()
							.getUpdatedCartIds().size());

					// TODO ensure parallel addition/won't hurt
					for (Long cartId : CartLogistics.getInstance().getUpdatedCartIds()) {
						
						int count = 0;
						
						
						Cart unsyncedCart = this.cartCache.get(cartId);
						Set<Long> barcodeKeys = unsyncedCart.getItemList().keySet();
						
						if(CollectionUtils.isEmpty(barcodeKeys)) {
							
							workerCartHandler.initWorkerCart(cartId, unsyncedCart.getBase().getVendorId());
							
						} else {

						for(Long barcode : barcodeKeys) {
							
								Integer earlierSyncedItemCount = 0;
								Integer newUnsyncedItemCount = unsyncedCart.getItemList().get(barcode).getCountForInStorePickup() + unsyncedCart.getItemList().get(barcode).getCountForDelivery();
							
								Map<Long, Integer> itemsToBeSynced = new HashMap<Long, Integer>();
								
								boolean isBarcodeSyncedEarlier = unsyncedCart.getAdminSyncedBarcodeCount().containsKey(barcode);
								
								if(isBarcodeSyncedEarlier) {
									earlierSyncedItemCount = unsyncedCart.getAdminSyncedBarcodeCount().get(barcode);
								}
								
								if(!isBarcodeSyncedEarlier || earlierSyncedItemCount.compareTo(newUnsyncedItemCount) != 0) {
									
									count++;
									Long missingItemBarcode = barcode;
									Integer deltaItemCount = newUnsyncedItemCount - earlierSyncedItemCount;
									
									// TODO Shukla!!! Remember to check for status & items check
									itemsToBeSynced.put(missingItemBarcode, deltaItemCount);
									workerCartHandler.addCartDelta(cartId, itemsToBeSynced);
									
									unsyncedCart.getAdminSyncedBarcodeCount().put(barcode, newUnsyncedItemCount);
									
								}
								
								if(unsyncedCart.getBase().getStatus() == CartStatus.IN_PREPARATION) {
									workerCartHandler.closeCart(cartId);
								}
								
								
							}
						}
						
						unsyncedCart.setCartSyncedWithAdmin(Boolean.TRUE);
						
						synchronized (cartId) {
							this.cartCache.put(cartId, unsyncedCart);
							
							CartLogistics.getInstance().getSyncedCartIds().add(cartId);
							CartLogistics.getInstance().getUpdatedCartIds().remove(cartId);
						}
						
						LOGGER.debug("Completed updating [{}] items of cart [{}] to the logistics service", count, cartId);

					}

					LOGGER.debug("Completed. New set of synced and updated carts are [{}] and [{}]", CartLogistics
							.getInstance().getSyncedCartIds().size(), CartLogistics.getInstance().getUpdatedCartIds()
							.size());

				}
				
				if (CartLogistics.getInstance().getClosedCartIds().size() > 0) {
					LOGGER.debug("Need to process [{}] closed carts...", CartLogistics.getInstance()
							.getClosedCartIds().size());

					int count = 0;

					// TODO ensure parallel addition/won't hurt
					for (Long cartId : CartLogistics.getInstance().getClosedCartIds()) {
						
						count++;
						Cart unsyncedCart = this.cartCache.get(cartId);
						
						workerCartHandler.closeCart(cartId);
						
						unsyncedCart.setCartSyncedWithAdmin(Boolean.TRUE);
						
						synchronized (cartId) {
							this.cartCache.put(cartId, unsyncedCart);
							
							CartLogistics.getInstance().getSyncedCartIds().add(cartId);
							CartLogistics.getInstance().getClosedCartIds().remove(cartId);
							CartLogistics.getInstance().getUpdatedCartIds().remove(cartId);
						}
						
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
