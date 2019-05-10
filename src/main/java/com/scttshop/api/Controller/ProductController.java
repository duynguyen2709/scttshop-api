package com.scttshop.api.Controller;

import com.scttshop.api.Entity.Product;
import com.scttshop.api.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class ProductController {

    @Autowired
    ProductRepository repo;

    @GetMapping("/products")
    List<Product> findAll(){
        return repo.findAll();
    }

    @GetMapping("/products/{id}")
    Product findById(@PathVariable("id") Integer id) {
        Optional<Product> product = repo.findById(id);

        return product.orElse(null);

    }
}
