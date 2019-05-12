package com.scttshop.api.Controller;

import com.scttshop.api.Entity.Category;
import com.scttshop.api.Entity.EmptyJsonResponse;
import com.scttshop.api.Entity.Product;
import com.scttshop.api.Repository.CategoryRepository;
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
public class CategoryController {

    @Autowired
    private CategoryRepository repo;

    @Autowired
    private ProductRepository repo2;

    @GetMapping("/categories")
    public List<Category> findAll(){
        return repo.findAll();
    }

    @GetMapping("/categories/{id}")
    ResponseEntity findById(@PathVariable("id") Integer id) {
        Optional<Category> category = repo.findById(id);

        if (category.isPresent())
            return new ResponseEntity(category, HttpStatus.OK);

        return new ResponseEntity(new EmptyJsonResponse(),HttpStatus.NOT_FOUND);

    }

    @GetMapping("/categories/{id}/products")
    List<Product> findListProductOfCategory(@PathVariable("id") Integer categoryID) {
        return repo2.findByCategoryID(categoryID);
    }
}