package com.example.helloworld;

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

import com.example.helloworld.auth.ExampleAuthenticator;
import com.example.helloworld.auth.ExampleAuthorizer;
import com.example.helloworld.cache.InitialSetup;
import com.example.helloworld.cache.DifferentialDataLoader;
import com.example.helloworld.cache.VendorVersionLoader;
import com.example.helloworld.cli.RenderCommand;
import com.example.helloworld.core.InventoryMaster;
import com.example.helloworld.core.InventoryResponse;
import com.example.helloworld.core.InventorySyncStatus;
import com.example.helloworld.core.ItemResponse;
import com.example.helloworld.core.Person;
import com.example.helloworld.core.ProductMaster;
import com.example.helloworld.core.Template;
import com.example.helloworld.core.User;
import com.example.helloworld.core.VendorItemHistory;
import com.example.helloworld.core.VendorItemMaster;
import com.example.helloworld.core.VendorVersionDetail;
import com.example.helloworld.core.VendorVersionDifferential;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.filter.DateRequiredFeature;
import com.example.helloworld.health.TemplateHealthCheck;
import com.example.helloworld.manager.InventoryCurator;
import com.example.helloworld.manager.InventoryEvaluator;
import com.example.helloworld.resources.FilteredResource;
import com.example.helloworld.resources.HelloWorldResource;
import com.example.helloworld.resources.InventoryResource;
import com.example.helloworld.resources.PeopleResource;
import com.example.helloworld.resources.PersonResource;
import com.example.helloworld.resources.ProtectedResource;
import com.example.helloworld.resources.ViewResource;
import com.example.helloworld.task.DaemonJob;
import com.example.helloworld.task.DataLoaderJob;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
	
	static Logger LOGGER = LoggerFactory.getLogger(HelloWorldApplication.class);
	
	public static void main(String[] args) throws Exception {
		new HelloWorldApplication().run(args);
	}

	private final HibernateBundle<HelloWorldConfiguration> hibernateBundle = new HibernateBundle<HelloWorldConfiguration>(
			Person.class, InventorySyncStatus.class, VendorItemMaster.class, VendorItemHistory.class,
			ProductMaster.class, ItemResponse.class, InventoryMaster.class, VendorVersionDetail.class,
			VendorVersionDifferential.class) {
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

		LOGGER.debug("Starting the application by processing any new inventory...");
		
		/*
		 * Meant for DB updates, basically seeting up new inventory if any
		 * (since the last similar process) in the master tables for all further
		 * processing.
		 * 
		 * TODO Ensure that multiple machines running same code don't make the
		 * data inconsistent. Although everything is under a transaction so
		 * unlikely, but need to double check.
		 */
		final InventoryCurator curator = new InventoryCurator(hibernateBundle.getSessionFactory());
		curator.processNewInventory();

		LOGGER.debug("Setting up caches for Vendor Version and Version Differential Data...");
		
		final LoadingCache<Long, Long> vendorVersionCache = CacheBuilder.newBuilder().build(new VendorVersionLoader(hibernateBundle.getSessionFactory()));
		final LoadingCache<String, InventoryResponse> differentialInventoryCache = CacheBuilder.newBuilder().build(new DifferentialDataLoader(hibernateBundle.getSessionFactory()));
		
		LOGGER.debug("Setting up the vendor-version metadata in DB based on latest inventory...");
		curator.processVendorVersionMeta(differentialInventoryCache, vendorVersionCache);

		/*
		 * Check the tables based on inventory updates and setup the caches for
		 * serving data.
		 * 
		 * TODO: This can possibly be removed now because InventoryCurator
		 * explicitly calls for cache updates also.
		 */
		final InitialSetup diffCacheSetup = new InitialSetup(hibernateBundle.getSessionFactory(), differentialInventoryCache, vendorVersionCache);
		diffCacheSetup.setupInitialCaches();
		
		LOGGER.debug("Done setting up differential cache with [{}] entries.", differentialInventoryCache.size());

		/*
		 * Contains the logic of serving the requests including latest vendor
		 * inventory and since a specific version
		 */
		final InventoryEvaluator inventoryEvaluator = new InventoryEvaluator(differentialInventoryCache, vendorVersionCache);
		
		LOGGER.debug("Setting up lifecycle for periodic DB updates based on new inventory...");
		environment.lifecycle().manage(new DaemonJob(hibernateBundle.getSessionFactory(), differentialInventoryCache, vendorVersionCache));
		LOGGER.debug("Setting up lifecycle for Version-Differential cache loaders...");
		environment.lifecycle().manage(new DataLoaderJob(hibernateBundle.getSessionFactory(), differentialInventoryCache, vendorVersionCache));
		LOGGER.debug("Completed seting up periodic cache updates...");

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

		environment.jersey().register(new InventoryResource(inventoryEvaluator));
		environment.jersey().register(new FilteredResource());
		LOGGER.debug("Initialisation complete.");
	}
}
