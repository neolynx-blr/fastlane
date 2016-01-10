package com.neolynks.curator;

import com.neolynks.worker.manager.WorkerCartHandler;
import com.neolynks.worker.manager.WorkerSessionHandler;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.neolynks.common.model.ItemResponse;
import com.neolynks.common.model.client.InventoryInfo;
import com.neolynks.common.model.client.ItemInfo;
import com.neolynks.common.model.client.price.DiscountDetail;
import com.neolynks.common.model.client.price.DiscountInfo;
import com.neolynks.common.model.client.price.TaxDetail;
import com.neolynks.common.model.client.price.TaxInfo;
import com.neolynks.common.model.order.CartRequest;
import com.neolynks.common.model.order.DeliveryMode;
import com.neolynks.common.model.order.ItemRequest;
import com.neolynks.curator.cache.CartCacheLoader;
import com.neolynks.curator.cache.CurrentInventoryLoader;
import com.neolynks.curator.cache.DifferentialDataLoader;
import com.neolynks.curator.cache.RecentItemLoader;
import com.neolynks.curator.cache.VendorInventoryLoader;
import com.neolynks.curator.cache.VendorVersionLoader;
import com.neolynks.curator.core.Account;
import com.neolynks.curator.core.InventoryMaster;
import com.neolynks.curator.core.OrderDetail;
import com.neolynks.curator.core.Person;
import com.neolynks.curator.core.ProductMaster;
import com.neolynks.curator.core.VendorItemHistory;
import com.neolynks.curator.core.VendorItemMaster;
import com.neolynks.curator.core.VendorVersionDetail;
import com.neolynks.curator.core.VendorVersionDifferential;
import com.neolynks.curator.db.InventoryMasterDAO;
import com.neolynks.curator.db.OrderDetailDAO;
import com.neolynks.curator.db.PersonDAO;
import com.neolynks.curator.db.ProductMasterDAO;
import com.neolynks.curator.db.VendorItemHistoryDAO;
import com.neolynks.curator.db.VendorItemMasterDAO;
import com.neolynks.curator.db.VendorVersionDetailDAO;
import com.neolynks.curator.db.VendorVersionDifferentialDAO;
import com.neolynks.curator.manager.AccountService;
import com.neolynks.curator.manager.CacheCurator;
import com.neolynks.curator.manager.CacheEvaluator;
import com.neolynks.curator.manager.CartHandler;
import com.neolynks.curator.manager.InventoryCurator;
import com.neolynks.curator.manager.InventoryEvaluator;
import com.neolynks.curator.manager.InventoryLoader;
import com.neolynks.curator.manager.OrderProcessor;
import com.neolynks.curator.manager.PriceEvaluator;
import com.neolynks.curator.manager.ProductMasterService;
import com.neolynks.curator.manager.VendorItemService;
import com.neolynks.curator.manager.VendorVersionDifferentialService;
import com.neolynks.curator.manager.VendorVersionService;
import com.neolynks.curator.model.Cart;
import com.neolynks.curator.resources.CacheResource;
import com.neolynks.curator.resources.CartResource;
import com.neolynks.curator.resources.OrderResource;
import com.neolynks.curator.resources.UserResource;
import com.neolynks.curator.resources.VendorResource;
import com.neolynks.curator.task.CartOperatorJob;
import com.neolynks.curator.task.DaemonJob;
import com.neolynks.curator.task.DataLoaderJob;
import com.neolynks.vendor.ClientResource;
import com.neolynks.vendor.job.InventorySync;
import com.neolynks.vendor.manager.InventoryService;

public class FastlaneApplication extends Application<FastlaneConfiguration> {

	static Logger LOGGER = LoggerFactory.getLogger(FastlaneApplication.class);

	public static void main(String[] args) throws Exception {
		new FastlaneApplication().run(args);
	}

	private final HibernateBundle<FastlaneConfiguration> hibernateBundle = new HibernateBundle<FastlaneConfiguration>(
			Person.class, VendorItemMaster.class, VendorItemHistory.class, ProductMaster.class, ItemResponse.class,
			InventoryMaster.class, VendorVersionDetail.class, VendorVersionDifferential.class, Account.class, OrderDetail.class) {
		@Override
		public DataSourceFactory getDataSourceFactory(FastlaneConfiguration configuration) {
			return configuration.getDataSourceFactory();
		}
	};

	@Override
	public String getName() {
		return "hello-world";
	}

	@Override
	public void initialize(Bootstrap<FastlaneConfiguration> bootstrap) {
		// Enable variable substitution with environment variables
		bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(bootstrap
				.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));

		bootstrap.addBundle(new AssetsBundle());
		bootstrap.addBundle(new MigrationsBundle<FastlaneConfiguration>() {
			@Override
			public DataSourceFactory getDataSourceFactory(FastlaneConfiguration configuration) {
				return configuration.getDataSourceFactory();
			}
		});
		bootstrap.addBundle(hibernateBundle);
		bootstrap.addBundle(new ViewBundle<FastlaneConfiguration>() {
			@Override
			public Map<String, Map<String, String>> getViewConfiguration(FastlaneConfiguration configuration) {
				return configuration.getViewRendererConfiguration();
			}
		});

	}

	@Override
	public void run(FastlaneConfiguration configuration, Environment environment) {

		LOGGER.info("Initialising server side, starting with setting up DAO classes & service layer for authentication and authorization...");
		final PersonDAO dao = new PersonDAO(hibernateBundle.getSessionFactory());
		final OrderDetailDAO orderDetailDAO = new OrderDetailDAO(hibernateBundle.getSessionFactory());
		final AccountService accountService = new AccountService(hibernateBundle.getSessionFactory());

		/**
		 * Currently same project is used for both server side as well as vendor
		 * client side execution. This particular configuration will accordingly
		 * define what really would be constructed and executed, taking away the
		 * immediate need to create 2 different code repositories. Both
		 * configurations being true is also supported and is how everything is being
		 * tested currently.
		 */
		
		if (configuration.getClientConfig()) {

			LOGGER.info("Starting server for vendor-side interactions...");

			LOGGER.info("Setting up the business-logic class followed by registering the inventory resource and it's lifecycle...");
			final InventoryService inventoryService = new InventoryService(configuration.getCurationConfig());
			environment.jersey().register(new ClientResource(inventoryService));
			environment.lifecycle().manage(new InventorySync(configuration.getCurationConfig()));

		}

		if (configuration.getServerConfig()) {
			
			LOGGER.info("Starting server for user-device interactions...");
			LOGGER.info("Setting up all the DAO classes...");

			final InventoryMasterDAO invMasterDAO = new InventoryMasterDAO(hibernateBundle.getSessionFactory());
			final ProductMasterDAO productMasterDAO = new ProductMasterDAO(hibernateBundle.getSessionFactory());
			final VendorItemMasterDAO vendorItemMasterDAO = new VendorItemMasterDAO(hibernateBundle.getSessionFactory());
			final VendorItemHistoryDAO vendorItemHistoryDAO = new VendorItemHistoryDAO(hibernateBundle.getSessionFactory());
			final VendorVersionDetailDAO vendorVersionDetailDAO = new VendorVersionDetailDAO(hibernateBundle.getSessionFactory());
			final VendorVersionDifferentialDAO vendorVersionDiffDAO = new VendorVersionDifferentialDAO(hibernateBundle.getSessionFactory());

			LOGGER.info("Setting up definitions for various caches for vendor,version,inventory, differential data points...");
			
			// Key: Cart-Id, Value: Cart Details
			final LoadingCache<Long, Cart> cartCache = CacheBuilder.newBuilder().build(new CartCacheLoader());
			
			// Key: Vendor-Id, Value: Latest known inventory version
			final LoadingCache<Long, Long> vendorVersionCache = CacheBuilder.newBuilder().build(new VendorVersionLoader(hibernateBundle.getSessionFactory()));

			// Key: Vendor-Id, Value: JSON for InventoryInfo indicating the most up-to-date inventory details
			final LoadingCache<Long, String> currentInventoryCache = CacheBuilder.newBuilder().build(new CurrentInventoryLoader(hibernateBundle.getSessionFactory()));

			// Key: Vendor-Id + - + Barcode, Value: Inventory updates for version-id in the key w.r.t. the latest known inventory for this vendor  
			final LoadingCache<String, ItemInfo> vendorBarcodeInventoryCache = CacheBuilder.newBuilder().build(new VendorInventoryLoader(hibernateBundle.getSessionFactory()));
			
			// Key: Vendor-Id + - + Version-Id, Value: Inventory updates for version-id in the key w.r.t. the latest known inventory for this vendor  
			final LoadingCache<String, InventoryInfo> differentialInventoryCache = CacheBuilder.newBuilder().build(new DifferentialDataLoader(hibernateBundle.getSessionFactory()));

			// Key: Vendor-Id + - + Version-Id, Value: Inventory updates for version-id in the key w.r.t. the latest known inventory for this vendor  
			//TODO final LoadingCache<String, > vendorInventoryCache = CacheBuilder.newBuilder().build(new DifferentialDataLoader(hibernateBundle.getSessionFactory()));

			final LoadingCache<String, InventoryInfo> recentItemsCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.DAYS).build(new RecentItemLoader(hibernateBundle.getSessionFactory()));

			LOGGER.info("Setting up various service classes, abstracting the DAO classes from the business logic objects...");
			final VendorVersionService vvDetailService = new VendorVersionService(vendorVersionDetailDAO);
			final VendorItemService vendorItemService = new VendorItemService(vendorItemMasterDAO, vendorItemHistoryDAO);
			final VendorVersionDifferentialService vvDiffService = new VendorVersionDifferentialService(vendorVersionDiffDAO);
			final ProductMasterService pmService = new ProductMasterService(hibernateBundle.getSessionFactory(), productMasterDAO);

			/*
			 * Contains the logic of serving the requests including latest
			 * vendor inventory and since a specific version
			 */
			LOGGER.info("Setting up various business-logic classes which actually dictates the business logic...");
			final InventoryCurator invCurator = new InventoryCurator(hibernateBundle.getSessionFactory());
			final CacheEvaluator cacheEvaluator = new CacheEvaluator(differentialInventoryCache, recentItemsCache, currentInventoryCache);
			final InventoryEvaluator inventoryEvaluator = new InventoryEvaluator(invMasterDAO, differentialInventoryCache, recentItemsCache, currentInventoryCache);

			/*
			 * Meant for DB updates, basically setting up new inventory if any
			 * (since the last similar process) in the master tables for all
			 * further processing.
			 * 
			 * TODO Ensure that multiple machines running same code don't make
			 * the data inconsistent. Although everything is under a transaction
			 * so unlikely, but need to double check.
			 */
			LOGGER.info("Quick look at DB for new inventory from vendor to setup the right inventory tables...");
			try {
				invCurator.processNewInventory();
				invCurator.processDifferentialData(differentialInventoryCache);
				invCurator.processVendorDetailData(vendorVersionCache, currentInventoryCache);
			} catch (Exception e) {
				LOGGER.warn("Exception [{}] with error message [{}] occurred while loading the new inventory and setting up right tables in DB during server start-up. \n"
						+ "Moving on for now given that this will be re-executed every few second.", e.getClass().getName(), e.getMessage());
				e.printStackTrace();
			}
			
			/**
			 * All the cart handlers including interfacing with the user
			 * device-app as well as the worker side cart operations.
			 */
			final WorkerSessionHandler workerSessionHandler = new WorkerSessionHandler();
			final WorkerCartHandler workerCartHandler = new WorkerCartHandler(workerSessionHandler);
			
			final CartHandler cartEvaluator = new CartHandler(cartCache, vendorVersionCache, orderDetailDAO);
			
			
			final PriceEvaluator priceEvaluator = new PriceEvaluator(vendorVersionCache, differentialInventoryCache);
			final OrderProcessor orderProcessor = new OrderProcessor(orderDetailDAO, vendorVersionCache, differentialInventoryCache, priceEvaluator, vendorBarcodeInventoryCache);
			

			/*
			 * Check the tables based on inventory updates and setup the caches
			 * for serving data.
			 * 
			 * This is required to be here given that application could have
			 * crashed last time in the middle or immediately post the DB
			 * operation etc.
			 */
			LOGGER.info("Based on latest DB updates for vendor, version, inventory, setting up the caches...");
			final CacheCurator cacheCurator = new CacheCurator(hibernateBundle.getSessionFactory(),
					differentialInventoryCache, vendorVersionCache, recentItemsCache, currentInventoryCache);
			cacheCurator.processVendorVersionCache();
			cacheCurator.processCurrentInventoryCache();
			cacheCurator.processRecentItemRecordsCache();
			cacheCurator.processDifferentialInventoryCache();
			
			final InventoryLoader inventoryLoader = new InventoryLoader(invMasterDAO, configuration.getCurationConfig(), cacheCurator, pmService, vvDiffService, vvDetailService, vendorItemService);

			LOGGER.debug(
					"Done setting up differential cache with entries [{}] for vendor-version, [{}] for current-inventory, [{}] for recent-items, [{}] for differential data.",
					vendorVersionCache.size(), currentInventoryCache.size(), recentItemsCache.size(),
					differentialInventoryCache.size());


			LOGGER.info("Setting up lifecycle for periodic inventory DB updates, corresponding updates for data and caches...");
			environment.lifecycle().manage(new DaemonJob(hibernateBundle.getSessionFactory(), differentialInventoryCache, vendorVersionCache, currentInventoryCache));
			environment.lifecycle().manage(new DataLoaderJob(differentialInventoryCache, vendorVersionCache, recentItemsCache, cacheCurator));
			environment.lifecycle().manage(new CartOperatorJob(cartCache));

			LOGGER.info("Registering the various resources with the runtime environment for serving...");
			environment.jersey().register(new CacheResource(cacheEvaluator));
			environment.jersey().register(new UserResource(inventoryEvaluator));
			environment.jersey().register(new VendorResource(inventoryEvaluator, inventoryLoader));
			environment.jersey().register(new OrderResource(orderProcessor));
			environment.jersey().register(new CartResource(cartEvaluator, orderProcessor));
			
		}

		temporaryCode();
		LOGGER.info("Initialisation complete.");
		
	}
	
	public void temporaryCode() {
		/************************************ Temporary Code *******************************************/
		
		try {
			DiscountInfo discount1 = new DiscountInfo();
			//discount1.setDiscountedItemCode("ITEM201");
			//discount1.setDiscountType(6);
			//discount1.setDiscountValue(2.0);
			//discount1.setRequiredCountForDiscount(8);
			
			DiscountInfo discount2 = new DiscountInfo();
			//discount1.setDiscountType(1);
			//discount1.setDiscountValue(20.0);

			DiscountDetail discountDetail = new DiscountDetail();
			discountDetail.getDiscountInfo().add(discount1);
			discountDetail.getDiscountInfo().add(discount2);
			
			ObjectMapper mapper = new ObjectMapper();
			try {
				System.out.println(mapper.writeValueAsString(discountDetail));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			
			TaxInfo tax1 = new TaxInfo();
			tax1.setTaxType(1);
			tax1.setTaxValue(20.0);
			
			TaxInfo tax2 = new TaxInfo();
			tax2.setTaxType(2);
			tax2.setTaxValue(10.0);
			
			TaxDetail taxDetail = new TaxDetail();
			taxDetail.getTaxInfo().add(tax1);
			taxDetail.getTaxInfo().add(tax2);
			
			try {
				System.out.println(mapper.writeValueAsString(taxDetail));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			
			CartRequest cart = new CartRequest();
			cart.setDeliveryMode(DeliveryMode.IN_STORE_PICKUP);
			cart.setDeviceDataVersionId(1448552860765L);
			cart.setVendorId(281L);
			
			ItemRequest firstItem = new ItemRequest();
			firstItem.setBarcode(8906004864247L);
			firstItem.setItemCode("B00E3QW6P4");
			firstItem.setCountForInStorePickup(2);
			
			ItemRequest secondItem = new ItemRequest();
			secondItem.setBarcode(8901030320491L);
			secondItem.setItemCode("B00791DDUM");
			secondItem.setCountForInStorePickup(2);
			/*
			cart.setItemList(new ArrayList<ItemRequest>());
			cart.getItemList().add(firstItem);
			cart.getItemList().add(secondItem);*/
			
			cart.setNetAmount(132.34D);
			
			System.out.println(mapper.writeValueAsString(cart));
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		/************************************ Temporary Code *******************************************/

	}
}
