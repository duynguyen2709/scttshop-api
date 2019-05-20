package com.scttshop.api.Controller;

import com.scttshop.api.Entity.*;
import com.scttshop.api.Repository.CategoryRepository;
import com.scttshop.api.Repository.ProductRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class CategoryController {

    @Autowired
    private CategoryRepository repo;

    @Autowired
    private ProductRepository repo2;

    @Autowired
    ProductController controller;

    @GetMapping("/categories")
    @Cacheable(value="categories",key="'all'")
    public List<Category> findAll(){

        return repo.findAll();
    }

    @GetMapping("/categories/{id}")
    @Cacheable(value="categories",key="#id")
    ResponseEntity findById(@PathVariable("id") Integer id) {
        Optional<Category> category = repo.findById(id);

        if (category.isPresent())
            return new ResponseEntity(category, HttpStatus.OK);

        return new ResponseEntity(new EmptyJsonResponse(),HttpStatus.NOT_FOUND);

    }

    @GetMapping("/categories/{id}/products")
    @Cacheable(value="categories",key="'products' + #id")
    List<DiscountProduct> findListProductOfCategory(@PathVariable("id") Integer categoryID) {
        final List<Product> all = repo2.findByCategoryID(categoryID);

        List<DiscountProduct> list = new ArrayList<>();

        for (Product prod: all){
            DiscountProduct discountProduct = new DiscountProduct(prod);

            Promotion promotion = controller.isOnPromotion(discountProduct.getProductID());

            if (promotion != null)
            {
                discountProduct.setPromotionDiscount(promotion.getPromotionDiscount());
                discountProduct.setDiscountPrice(discountProduct.getSellPrice() - discountProduct.getSellPrice()*discountProduct.getPromotionDiscount()/100);
            }
            else {
                discountProduct.setDiscountPrice(discountProduct.getSellPrice());
                discountProduct.setPromotionDiscount(0);
            }

            list.add(discountProduct);
        }

        return list;
    }

    @PostMapping("/categories")
    @Caching(
            put= { @CachePut(value= "categories", key= "#category.categoryID") },
            evict= { @CacheEvict(value= "categories", key="'all'")}
    )
    public ResponseEntity insertCategory(@Valid @RequestBody Category category){

        try{
            category.setCategoryID(0);
            category.setUpdDate(new Timestamp(System.currentTimeMillis()));
            Category res = repo.save(category);

            if (res == null)
                throw new Exception();

            return new ResponseEntity(res,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/categories/{id}")
    @Caching(
            put= { @CachePut(value= "categories", key= "#id") },
            evict= { @CacheEvict(value= "categories", key="'all'")}
    )
    public ResponseEntity updateCategory(@PathVariable(value = "id") Integer id,
                                        @Valid @RequestBody Category category){
        try{
            Optional<Category> old = repo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            old.get().copyFieldValues(category);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));
            Category updatedProduct = repo.save(old.get());

            if (updatedProduct == null)
                throw new Exception();

            return new ResponseEntity(updatedProduct,HttpStatus.OK);

        }
        //        catch (ChangeSetPersister.NotFoundException e){
        //            System.out.println(e.getMessage());
        //            return new ResponseEntity(new EmptyJsonResponse(),HttpStatus.NOT_FOUND);
        //        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/categories/{id}")
    @Caching(
            evict= {
                    @CacheEvict(value="categories",key="#id"),
                    @CacheEvict(value= "categories", key="'all'")
            }
    )
    public ResponseEntity deleteCategory(@PathVariable(value = "id") Integer id){

        try{
            Optional<Category> old = repo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            repo.delete(old.get());

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
