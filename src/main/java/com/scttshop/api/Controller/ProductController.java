package com.scttshop.api.Controller;

import com.scttshop.api.Entity.EmptyJsonResponse;
import com.scttshop.api.Entity.Product;
import com.scttshop.api.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class ProductController {

    @Autowired
    private ProductRepository repo;

    @GetMapping("/products")
    List<Product> findAll(){
        return repo.findAll();
    }

    @GetMapping("/products/{id}")
    ResponseEntity findById(@PathVariable("id") Integer id) {
        Optional<Product> product = repo.findById(id);

        if (product.isPresent())
            return new ResponseEntity(product, HttpStatus.OK);

        return new ResponseEntity(new EmptyJsonResponse(),HttpStatus.NOT_FOUND);

    }
}
