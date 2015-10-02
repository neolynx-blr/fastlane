package com.neolynx.curator;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

import java.util.Map;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.InventoryResponse;
import com.neolynx.common.model.ItemResponse;
import com.neolynx.curator.auth.ExampleAuthenticator;
import com.neolynx.curator.auth.ExampleAuthorizer;
import com.neolynx.curator.cache.DifferentialDataLoader;
import com.neolynx.curator.cache.VendorVersionLoader;
import com.neolynx.curator.cli.RenderCommand;
import com.neolynx.curator.core.InventoryMaster;
import com.neolynx.curator.core.Person;
import com.neolynx.curator.core.ProductMaster;
import com.neolynx.curator.core.Template;
import com.neolynx.curator.core.User;
import com.neolynx.curator.core.VendorItemHistory;
import com.neolynx.curator.core.VendorItemMaster;
import com.neolynx.curator.core.VendorVersionDetail;
import com.neolynx.curator.core.VendorVersionDifferential;
import com.neolynx.curator.db.InventoryMasterDAO;
import com.neolynx.curator.db.PersonDAO;
import com.neolynx.curator.filter.DateRequiredFeature;
import com.neolynx.curator.health.TemplateHealthCheck;
import com.neolynx.curator.manager.CacheCurator;
import com.neolynx.curator.manager.InventoryCurator;
import com.neolynx.curator.manager.InventoryEvaluator;
import com.neolynx.curator.manager.InventoryLoader;
import com.neolynx.curator.resources.FilteredResource;
import com.neolynx.curator.resources.HelloWorldResource;
import com.neolynx.curator.resources.InventoryResource;
import com.neolynx.curator.resources.PeopleResource;
import com.neolynx.curator.resources.PersonResource;
import com.neolynx.curator.resources.ProtectedResource;
import com.neolynx.curator.resources.ViewResource;
import com.neolynx.curator.task.DaemonJob;
import com.neolynx.curator.task.DataLoaderJob;
import com.neolynx.vendor.ClientResource;
import com.neolynx.vendor.job.InventorySync;
import com.neolynx.vendor.manager.InventoryService;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

	static Logger LOGGER = LoggerFactory.getLogger(HelloWorldApplication.class);

	public static void main(String[] args) throws Exception {
		new HelloWorldApplication().run(args);
	}

	private final HibernateBundle<HelloWorldConfiguration> hibernateBundle = new HibernateBundle<HelloWorldConfiguration>(
			Person.class, VendorItemMaster.class, VendorItemHistory.class, ProductMaster.class, ItemResponse.class,
			InventoryMaster.class, VendorVersionDetail.class, VendorVersionDifferential.class) {
		@Override
		public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
			return configuration.getDataSourceFactory();
		}
	};

	@Override
	public String getName() {
		return "hello-world";
	}

	@Override
	public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
		// Enable variable substitution with environment variables
		bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(bootstrap
				.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));

		bootstrap.addCommand(new RenderCommand());
		bootstrap.addBundle(new AssetsBundle());
		bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>() {
			@Override
			public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
				return configuration.getDataSourceFactory();
			}
		});
		bootstrap.addBundle(hibernateBundle);
		bootstrap.addBundle(new ViewBundle<HelloWorldConfiguration>() {
			@Override
			public Map<String, Map<String, String>> getViewConfiguration(HelloWorldConfiguration configuration) {
				return configuration.getViewRendererConfiguration();
			}
		});

	}

	@Override
	public void run(HelloWorldConfiguration configuration, Environment environment) {

		final PersonDAO dao = new PersonDAO(hibernateBundle.getSessionFactory());

		/**
		 * Currently same project is used for both server side as well as vendor
		 * client side execution. This particular configuration will accordingly
		 * define what really would be constructed and executed, taking away the
		 * immediate need to create 2 different code repositories. Both
		 * configurations being is also supported and is how everything is being
		 * tested currently.
		 */

		if (configuration.getClientConfig()) {

			LOGGER.debug("Setting up the project for vendor client side configuration...");

			final InventoryService inventoryService = new InventoryService(configuration.getCurationConfig());
			environment.jersey().register(new ClientResource(inventoryService));

			LOGGER.debug("Setting up lifecycle for Inventory loader...");
			environment.lifecycle().manage(new InventorySync(configuration.getCurationConfig()));

		}

		if (configuration.getServerConfig()) {
			
			LOGGER.debug("Setting up the project for server side configuration...");
			
			final InventoryCurator invCurator = new InventoryCurator(hibernateBundle.getSessionFactory());
			final InventoryMasterDAO invMasterDAO = new InventoryMasterDAO(hibernateBundle.getSessionFactory());


			/*
			 * Meant for DB updates, basically setting up new inventory if any
			 * (since the last similar process) in the master tables for all
			 * further processing.
			 * 
			 * TODO Ensure that multiple machines running same code don't make
			 * the data inconsistent. Although everything is under a transaction
			 * so unlikely, but need to double check.
			 */

			
			LOGGER.debug("Starting the application by processing any new inventory...");
			invCurator.processNewInventory();

			LOGGER.debug("Setting up caches for Vendor Version and Version Differential Data...");
			final LoadingCache<Long, Long> vendorVersionCache = CacheBuilder.newBuilder().build(new VendorVersionLoader(hibernateBundle.getSessionFactory()));
			final LoadingCache<String, InventoryResponse> differentialInventoryCache = CacheBuilder.newBuilder().build(new DifferentialDataLoader(hibernateBundle.getSessionFactory()));

			LOGGER.debug("Setting up the vendor-version metadata in DB based on latest inventory...");
			invCurator.processVendorVersionMeta(differentialInventoryCache, vendorVersionCache);

			/*
			 * Check the tables based on inventory updates and setup the caches
			 * for serving data.
			 * 
			 * This is required to be here given that application could have
			 * crashed last time in the middle or immediately post the DB
			 * operation etc.
			 */
			final CacheCurator cacheCurator = new CacheCurator(hibernateBundle.getSessionFactory(), differentialInventoryCache, vendorVersionCache);
			cacheCurator.processVendorVersionCache();
			cacheCurator.processDifferentialInventoryCache();

			LOGGER.debug("Done setting up differential cache with [{}] entries.", differentialInventoryCache.size());

			/*
			 * Contains the logic of serving the requests including latest
			 * vendor inventory and since a specific version
			 */
			final InventoryLoader inventoryLoader = new InventoryLoader(invMasterDAO);
			final InventoryEvaluator inventoryEvaluator = new InventoryEvaluator(differentialInventoryCache, vendorVersionCache);

			LOGGER.debug("Setting up lifecycle for periodic DB updates based on new inventory...");
			environment.lifecycle().manage(new DaemonJob(hibernateBundle.getSessionFactory(), differentialInventoryCache, vendorVersionCache));
			LOGGER.debug("Setting up lifecycle for Version-Differential cache loaders...");
			environment.lifecycle().manage(new DataLoaderJob(differentialInventoryCache, vendorVersionCache, cacheCurator));

			LOGGER.debug("Completed seting up periodic cache updates...");
			environment.jersey().register(new InventoryResource(inventoryEvaluator, inventoryLoader));
		}

		final Template template = configuration.buildTemplate();

		environment.healthChecks().register("template", new TemplateHealthCheck(template));
		environment.jersey().register(DateRequiredFeature.class);
		environment.jersey().register(
				new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
						.setAuthenticator(new ExampleAuthenticator()).setAuthorizer(new ExampleAuthorizer())
						.setRealm("SUPER SECRET STUFF").buildAuthFilter()));

		environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
		environment.jersey().register(RolesAllowedDynamicFeature.class);
		environment.jersey().register(new HelloWorldResource(template));
		environment.jersey().register(new ViewResource());
		environment.jersey().register(new ProtectedResource());

		environment.jersey().register(new PeopleResource(dao));
		environment.jersey().register(new PersonResource(dao));

		environment.jersey().register(new FilteredResource());
		LOGGER.debug("Initialisation complete.");
	}
}

/**
 *  Discarded code
 *  
 *  			ItemResponse itemResponse = new ItemResponse();
			itemResponse.setBarcode(1251L);
			itemResponse.setDescription("Updated item via post call");
			itemResponse.setItemCode("I2001");
			itemResponse.setMrp(2.35);
			itemResponse.setName("Name of Post Updated item");
			itemResponse.setPrice(2.35);
			itemResponse.setTagline("Sample Tagline");
			itemResponse.setVersionId(1L);

			InventoryResponse response = new InventoryResponse();
			response.setNewDataVersionId(1L);
			response.setVendorId(71L);
			response.setItemsUpdated(new ArrayList<ItemResponse>());
			response.getItemsUpdated().add(itemResponse);
			response.setIsError(Boolean.FALSE);

			ObjectMapper mapper = new ObjectMapper();
			try {
				System.out.println(mapper.writeValueAsString(response));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 * 
 */
