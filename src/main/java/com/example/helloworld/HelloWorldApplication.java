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

import com.example.helloworld.auth.ExampleAuthenticator;
import com.example.helloworld.auth.ExampleAuthorizer;
import com.example.helloworld.cli.RenderCommand;
import com.example.helloworld.core.InventoryMaster;
import com.example.helloworld.core.InventorySyncStatus;
import com.example.helloworld.core.ItemResponse;
import com.example.helloworld.core.Person;
import com.example.helloworld.core.ProductMaster;
import com.example.helloworld.core.Template;
import com.example.helloworld.core.User;
import com.example.helloworld.core.VendorItemMaster;
import com.example.helloworld.db.InventoryMasterDAO;
import com.example.helloworld.db.InventorySyncStatusDAO;
import com.example.helloworld.db.ItemResponseDAO;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.db.ProductMasterDAO;
import com.example.helloworld.db.VendorDAO;
import com.example.helloworld.db.VendorItemMasterDAO;
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

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
	public static void main(String[] args) throws Exception {
		new HelloWorldApplication().run(args);
	}

	private final HibernateBundle<HelloWorldConfiguration> hibernateBundle = new HibernateBundle<HelloWorldConfiguration>(
			Person.class, InventorySyncStatus.class, VendorItemMaster.class, ProductMaster.class,
			ItemResponse.class, InventoryMaster.class) {
		@Override
		public DataSourceFactory getDataSourceFactory(
				HelloWorldConfiguration configuration) {
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
		bootstrap
				.setConfigurationSourceProvider(new SubstitutingSourceProvider(
						bootstrap.getConfigurationSourceProvider(),
						new EnvironmentVariableSubstitutor(false)));

		bootstrap.addCommand(new RenderCommand());
		bootstrap.addBundle(new AssetsBundle());
		bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>() {
			@Override
			public DataSourceFactory getDataSourceFactory(
					HelloWorldConfiguration configuration) {
				return configuration.getDataSourceFactory();
			}
		});
		bootstrap.addBundle(hibernateBundle);
		bootstrap.addBundle(new ViewBundle<HelloWorldConfiguration>() {
			@Override
			public Map<String, Map<String, String>> getViewConfiguration(
					HelloWorldConfiguration configuration) {
				return configuration.getViewRendererConfiguration();
			}
		});
	}

	@Override
	public void run(HelloWorldConfiguration configuration,
			Environment environment) {

		final PersonDAO dao = new PersonDAO(hibernateBundle.getSessionFactory());
		final VendorDAO vendorDAO = new VendorDAO(
				hibernateBundle.getSessionFactory());
		final InventoryMasterDAO allInventoryDAO = new InventoryMasterDAO(
				hibernateBundle.getSessionFactory());
		final InventorySyncStatusDAO inventorySyncDAO = new InventorySyncStatusDAO(
				hibernateBundle.getSessionFactory());

		final ProductMasterDAO productCoreDAO = new ProductMasterDAO(
				hibernateBundle.getSessionFactory());
		final VendorItemMasterDAO itemDetailDAO = new VendorItemMasterDAO(
				hibernateBundle.getSessionFactory());

		final ItemResponseDAO inventoryDAO = new ItemResponseDAO(
				hibernateBundle.getSessionFactory());

		final InventoryCurator inventoryCurator = new InventoryCurator(
				vendorDAO, inventorySyncDAO, allInventoryDAO,
				hibernateBundle.getSessionFactory(), productCoreDAO,
				itemDetailDAO);
		//inventoryCurator.prepareInventory();

		final InventoryEvaluator inventoryEvaluator = new InventoryEvaluator(inventoryDAO);

		final Template template = configuration.buildTemplate();

		environment.healthChecks().register("template",
				new TemplateHealthCheck(template));
		environment.jersey().register(DateRequiredFeature.class);
		environment.jersey().register(
				new AuthDynamicFeature(
						new BasicCredentialAuthFilter.Builder<User>()
								.setAuthenticator(new ExampleAuthenticator())
								.setAuthorizer(new ExampleAuthorizer())
								.setRealm("SUPER SECRET STUFF")
								.buildAuthFilter()));
		environment.jersey().register(
				new AuthValueFactoryProvider.Binder<>(User.class));
		environment.jersey().register(RolesAllowedDynamicFeature.class);
		environment.jersey().register(new HelloWorldResource(template));
		environment.jersey().register(new ViewResource());
		environment.jersey().register(new ProtectedResource());

		environment.jersey().register(new PeopleResource(dao));
		environment.jersey().register(new PersonResource(dao));

		environment.jersey()
				.register(new InventoryResource(inventoryEvaluator, inventoryCurator));
		environment.jersey().register(new FilteredResource());
	}
}
