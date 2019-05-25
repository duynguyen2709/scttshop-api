package com.scttshop.api.Controller;

import com.scttshop.api.Cache.CacheFactoryManager;
import com.scttshop.api.Entity.Customer;
import com.scttshop.api.Entity.EmptyJsonResponse;
import com.scttshop.api.Repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.scttshop.api.Cache.CacheFactoryManager.*;

@RestController public class CustomerController {

    @Autowired
    private CustomerRepository repo;

    @Autowired
    private EntityManager em;

    @GetMapping("/customers")
    //@Cacheable(value="customers", key="'all'")
    public List<Customer> getListCustomer() {

        if (CUSTOMER_CACHE != null) {
            return new ArrayList<>(CUSTOMER_CACHE.values());
        }

        try {
            return repo.findAll();
        }
        catch (Exception e) {
            System.out.println(String.format("CustomerController getListProduct ex: %s", e.getMessage()));
            return Collections.emptyList();
        }
    }

    @GetMapping("/customers/{email}")
    //@Cacheable(value="customers",key="#email")
    public ResponseEntity findById(@PathVariable("email") String email) {
        try {

            if (CUSTOMER_CACHE != null && CUSTOMER_CACHE.contains(email)) {
                return new ResponseEntity(CUSTOMER_CACHE.get(email), HttpStatus.OK);
            }

            Optional<Customer> customer = repo.findById(email);

            if (customer.isPresent()) {
                CUSTOMER_CACHE.putIfAbsent(email, customer.get());
                return new ResponseEntity(customer, HttpStatus.OK);
            }

            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

        }
        catch (Exception e) {
            System.out.println(String.format("CustomerController findById ex: %s", e.getMessage()));
            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/customers")
    //    @Caching(
    //            put= { @CachePut(value= "customers", key= "#customer.email") },
    //            evict= { @CacheEvict(value= "customers", key="'all'")}
    //    )
    public ResponseEntity insertCustomer(@Valid @RequestBody Customer customer) {

        try {
            if (CUSTOMER_CACHE.contains(customer.getEmail()) || repo.findById(customer.getEmail()).isPresent()){
                return new ResponseEntity("Email Already Existed.",HttpStatus.OK);
            }

            customer.setUpdDate(new Timestamp(System.currentTimeMillis()));
            Customer res = repo.save(customer);

            if (res == null) {
                throw new Exception();
            }

            // INSERT CACHE
            CUSTOMER_CACHE.put(res.getEmail(), res);

            return new ResponseEntity(res, HttpStatus.OK);
        }
        catch (Exception e) {
            System.out.println(String.format("CustomerController insertCustomer ex: %s", e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/customers/{email}")
    //    @Caching(
    //            put= { @CachePut(value= "customers", key= "#email") },
    //            evict= { @CacheEvict(value= "customers", key="'all'")}
    //    )
    public ResponseEntity updateCustomer(@PathVariable(value = "email") String email,
                                         @Valid @RequestBody Customer customer) {
        try {
            Optional<Customer> old = repo.findById(email);

            if (!old.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            old.get().copyFieldValues(customer);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));

            Customer updatedUser = repo.save(old.get());

            if (updatedUser == null) {
                throw new Exception();
            }

            CUSTOMER_CACHE.replace(updatedUser.getEmail(), updatedUser);

            return new ResponseEntity(updatedUser, HttpStatus.OK);

        }
        catch (Exception e) {
            System.out.println(String.format("CustomerController updateCustomer ex: %s", e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/customers/{email}/verify")
    //    @Caching(
    //            put= { @CachePut(value= "customers", key= "#email") },
    //            evict= { @CacheEvict(value= "customers", key="'all'")}
    //    )
    public ResponseEntity verifyCustomer(@PathVariable(value = "email") String email) {
        try {
            CUSTOMER_CACHE.get(email).setVerified(true);

            Optional<Customer> old = repo.findById(email);

            if (old.isPresent()) {
                old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));
                old.get().setVerified(true);

                Customer updatedUser = repo.save(old.get());

                if (updatedUser == null) {
                    throw new Exception();
                }
            }

            return new ResponseEntity(CUSTOMER_CACHE.get(email), HttpStatus.OK);

        }
        catch (Exception e) {
            System.out.println(String.format("CustomerController verifyCustomer ex: %s", e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/customers/{email}")
    //    @Caching(
    //            evict= {
    //                    @CacheEvict(value="customers",key="#email"),
    //                    @CacheEvict(value= "customers", key="'all'")
    //            }
    //    )
    public ResponseEntity deleteCustomer(@PathVariable(value = "email") String email) {

        try {
            Optional<Customer> old = repo.findById(email);

            if (!old.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            repo.delete(old.get());

            CUSTOMER_CACHE.remove(email);

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e) {
            System.out.println(String.format("CustomerController deleteCustomer ex: %s", e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
