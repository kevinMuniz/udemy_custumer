package com.lambdasoft.repository;

import com.lambdasoft.entities.Customer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CustomerRepository {

    @Inject
    EntityManager em;

    @Transactional
    public void create(Customer customer) {
        this.em.persist(customer);
    }

    @Transactional
    public void update(Customer customer) {
        this.em.merge(customer);
    }

    @Transactional
    public void delete(Customer customer) {
        this.em.remove(customer);
    }

    public List<Customer> getAll() {
        return this.em.createQuery("SELECT p FROM Customer p", Customer.class).getResultList();
    }

    public Customer findById(Long id) {
        return this.em.createQuery("SELECT p FROM Customer p WHERE p.id = :idParam", Customer.class)
                .setParameter("idParam", id).getSingleResult();
    }
    
}
