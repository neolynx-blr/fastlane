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
import com.example.helloworld.core.AllInventory;
import com.example.helloworld.core.Inventory;
import com.example.helloworld.core.ItemCore;
import com.example.helloworld.core.ItemDetail;
import com.example.helloworld.core.Person;
import com.example.helloworld.core.Product;
import com.example.helloworld.core.ProductCore;
import com.example.helloworld.core.Template;
import com.example.helloworld.core.User;
import com.example.helloworld.db.AllInventoryDAO;
import com.example.helloworld.db.InventoryDAO;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.db.ProductDAO;
import com.example.helloworld.filter.DateRequiredFeature;
import com.example.helloworld.health.TemplateHealthCheck;
import com.example.helloworld.manager.InventoryEvaluator;
import com.example.helloworld.resources.FilteredResource;
import com.example.helloworld.resources.HelloWorldResource;
import com.example.helloworld.resources.InventoryResource;
import com.example.helloworld.resources.PeopleResource;
import com.example.helloworld.resources.PersonResource;
import com.example.helloworld.resources.ProductResource;
import com.example.helloworld.resources.ProtectedResource;
import com.example.helloworld.resources.ViewResource;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

	private final HibernateBundle<HelloWorldConfiguration> hibernateBundle = new HibernateBundle<HelloWorldConfiguration>(
			Person.class, Product.class, ItemCore.class, ItemDetail.class, ProductCore.class, Inventory.class, AllInventory.class) {
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
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

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
        final ProductDAO productDAO = new ProductDAO(hibernateBundle.getSessionFactory());
        final AllInventoryDAO allInventoryDAO = new AllInventoryDAO(hibernateBundle.getSessionFactory());
        
        final InventoryDAO inventoryDAO = new InventoryDAO(hibernateBundle.getSessionFactory());
        final InventoryEvaluator inventoryEvaluator = new InventoryEvaluator(inventoryDAO, allInventoryDAO);
        
        final Template template = configuration.buildTemplate();

        environment.healthChecks().register("template", new TemplateHealthCheck(template));
        environment.jersey().register(DateRequiredFeature.class);
        environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                .setAuthenticator(new ExampleAuthenticator())
                .setAuthorizer(new ExampleAuthorizer())
                .setRealm("SUPER SECRET STUFF")
                .buildAuthFilter()));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new HelloWorldResource(template));
        environment.jersey().register(new ViewResource());
        environment.jersey().register(new ProtectedResource());
        
        environment.jersey().register(new PeopleResource(dao));
        environment.jersey().register(new PersonResource(dao));
        
        environment.jersey().register(new ProductResource(productDAO));
        
        environment.jersey().register(new InventoryResource(inventoryEvaluator));
        environment.jersey().register(new FilteredResource());
    }
}
