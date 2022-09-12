package com.lambdasoft.controller;

import com.lambdasoft.entities.Customer;
import com.lambdasoft.repository.CustomerRepository;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("customer")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerApi {

    @Inject
    CustomerRepository customerRepository;

    @GET
    public List<Customer> list() {
        return this.customerRepository.getAll();
    }

    @GET
    @Path("{idCustomer}")
    public Customer find(@PathParam("idCustomer") Long id) {
        return this.customerRepository.findById(id);
    }

    @POST
    public Response add(Customer customer) {
        this.customerRepository.create(customer);
        return Response.ok().build();
    }

    @PUT
    public Response update(Customer customer) {
        this.customerRepository.update(customer);
        return Response.ok().build();
    }

    @DELETE
    public Response remove(Customer customer) {
        this.customerRepository.delete(customer);
        return Response.ok().build();
    }

}
