package com.example.helloworld.resources;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.helloworld.core.Product;
import com.example.helloworld.db.ProductDAO;

@Path("/product")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

    private final ProductDAO productDAO;

    public ProductResource(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @POST
    @UnitOfWork
    public Product createProduct(Product product) {
        return productDAO.create(product);
    }

    @GET
    @UnitOfWork
    public List<Product> listProduct() {
        return productDAO.findAll();
    }
}
