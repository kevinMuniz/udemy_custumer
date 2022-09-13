package com.lambdasoft.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdasoft.entities.Customer;
import com.lambdasoft.entities.Product;
import com.lambdasoft.repository.CustomerRepository;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Path("customer")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerApi {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    Vertx vertx;

    private WebClient webClient;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    void initialize() {
        this.webClient = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(8081)
                .setSsl(false)
                .setTrustAll(true));
    }

    @GET
    @Blocking
    public List<Customer> list() {
        return this.customerRepository.getAll();
    }

    @GET
    @Blocking
    @Path("{idCustomer}")
    public Customer find(@PathParam("idCustomer") Long id) {
        return this.customerRepository.findById(id);
    }


    @GET
    @Blocking
    @Path("{idCustomer}/product")
    public Uni<Customer> getByIdProduct(@PathParam("idCustomer") Long id) {
        return Uni.combine().all().unis(this.getCustomerReactive(id), this.getAllProduct())
                .combinedWith((v1, v2)-> {
                   v1.getProducts().forEach(product -> v2.forEach(p-> {
                       if(Objects.equals(product.getProduct(),p.getId())) {
                           product.setName(p.getName());
                           product.setDescripcion(p.getName());
                       }
                   }));
                   return v1;
                });
    }

    @POST
    @Blocking
    public Response add(Customer customer) {
        this.customerRepository.create(customer);
        return Response.ok().build();
    }

    @PUT
    @Blocking
    public Response update(Customer param) {
        Customer customer = this.customerRepository.findById(param.getId());
        customer.setCode(param.getCode());
        customer.setAccountNumber(param.getAccountNumber());
        customer.setSurname(param.getSurname());
        customer.setPhone(param.getPhone());
        customer.setAddress(param.getAddress());
        customer.setProducts(param.getProducts());
        this.customerRepository.update(customer);
        return Response.ok().build();
    }

    @DELETE
    @Blocking
    public Response remove(Customer customer) {
        this.customerRepository.delete(customer);
        return Response.ok().build();
    }

    private Uni<Customer> getCustomerReactive(Long id) {
        return Uni.createFrom().item(this.customerRepository.findById(id));
    }

    private Uni<List<Product>> getAllProduct() {
        return this.webClient.get(8081, "localhost", "/product").send()
                .onFailure().invoke(resp->log.error("Error recuperando productos", resp))
                .onItem().transform(resp-> {
                    List<Product> products = new ArrayList<>();
                    resp.bodyAsJsonArray().forEach(p-> {
                        try {
                            products.add(new ObjectMapper().readValue(p.toString(), Product.class));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    });
                    return products;
                });
    }

}
