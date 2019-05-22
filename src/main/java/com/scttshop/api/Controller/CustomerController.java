package com.scttshop.api.Controller;

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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
public class CustomerController {

    @Autowired
    private CustomerRepository repo;

    @Autowired
    private EntityManager em;

    @GetMapping("/customers")
    @Cacheable(value="customers", key="'all'")
    public List<Customer> getListCustomer() {

        try {
            return repo.findAll();
        }
        catch (Exception e){
            System.out.println(String.format("CustomerController findAll ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }

    @GetMapping("/customers/{email}")
    @Cacheable(value="customers",key="#email")
    public ResponseEntity findById(@PathVariable("email") String email) {
        try {
            Optional<Customer> customer = repo.findById(email);

            if (customer.isPresent()) {
                return new ResponseEntity(customer, HttpStatus.OK);
            }

            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.NOT_FOUND);

        }
        catch (Exception e){
            System.out.println(String.format("CustomerController findById ex: %s" , e.getMessage()));
            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/customers")
    @Caching(
            put= { @CachePut(value= "customers", key= "#customer.email") },
            evict= { @CacheEvict(value= "customers", key="'all'")}
    )
    public ResponseEntity insertCustomer(@Valid @RequestBody Customer customer){

        try{
            customer.setUpdDate(new Timestamp(System.currentTimeMillis()));
            Customer res = repo.save(customer);

            if (res == null)
                throw new Exception();

            return new ResponseEntity(res,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("CustomerController insertCustomer ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/customers/{email}")
    @Caching(
            put= { @CachePut(value= "customers", key= "#email") },
            evict= { @CacheEvict(value= "customers", key="'all'")}
    )
    public ResponseEntity updateCustomer(@PathVariable(value = "email") String email,
                                            @Valid @RequestBody Customer customer){
        try{
            Optional<Customer> old = repo.findById(email);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            old.get().copyFieldValues(customer);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));

            Customer updatedUser = repo.save(old.get());

            if (updatedUser == null)
                throw new Exception();

            return new ResponseEntity(updatedUser,HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("CustomerController updateCustomer ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/customers/{email}")
    @Caching(
            evict= {
                    @CacheEvict(value="customers",key="#email"),
                    @CacheEvict(value= "customers", key="'all'")
            }
    )
    public ResponseEntity deleteCustomer(@PathVariable(value = "email") String email){

        try{
            Optional<Customer> old = repo.findById(email);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            repo.delete(old.get());

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("CustomerController deleteCustomer ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
