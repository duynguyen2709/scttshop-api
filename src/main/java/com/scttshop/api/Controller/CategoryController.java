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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.scttshop.api.Cache.CacheFactoryManager.CATEGORY_CACHE;
import static com.scttshop.api.Cache.CacheFactoryManager.PRODUCT_CACHE;

@RestController
public class CategoryController {

    @Autowired
    private CategoryRepository repo;

    @Autowired
    private ProductRepository repo2;

    @Autowired
    private ProductController controller;

    @GetMapping("/categories")
    public List<Category> getListCategories(){
        try {
            if (CATEGORY_CACHE != null){
                return new ArrayList<>(CATEGORY_CACHE.values());
            }

            return repo.findAll();
        }
        catch (Exception e){
            System.out.println(String.format("CategoryController getListProduct ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }

    @GetMapping("/categories/{id}")
    //@Cacheable(value="categories",key="#id")
    ResponseEntity findById(@PathVariable("id") Integer id) {
        try {
            if (CATEGORY_CACHE!= null) {

                Category category = CATEGORY_CACHE.get(id);
                return category == null ? new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK)
                        :new ResponseEntity(category, HttpStatus.OK);

            }

            Optional<Category> category = repo.findById(id);

            if (category.isPresent()) {
                CATEGORY_CACHE.putIfAbsent(id,category.get());
                return new ResponseEntity(category.get(), HttpStatus.OK);
            }

            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("CategoryController findById ex: %s" , e.getMessage()));
            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/categories/{id}/products")
    List<DiscountProduct> findListProductOfCategory(@PathVariable("id") Integer categoryID,@RequestParam(required = false,defaultValue = "true") Boolean isActive) {
        try {
            if (PRODUCT_CACHE != null){
                List<DiscountProduct> products = new ArrayList<>(PRODUCT_CACHE.values())
                                                .stream()
                                                .filter(p->p.getCategoryID() == categoryID)
                                                .collect(Collectors.toList());

                if (isActive != null && isActive)
                    products = products.stream().filter(c -> c.getIsActive() == 1).collect(Collectors.toList());

                return products;
            }

            final List<Product> all = repo2.findByCategoryID(categoryID);

            List<DiscountProduct> list = new ArrayList<>();

            for (Product prod : all) {
                DiscountProduct discountProduct = new DiscountProduct(prod);

                Promotion promotion = controller.isOnPromotion(discountProduct.getProductID());

                if (promotion != null) {
                    discountProduct.setPromotionDiscount(promotion.getPromotionDiscount());
                    discountProduct.setDiscountPrice(discountProduct.getSellPrice() -
                            discountProduct.getSellPrice() * discountProduct.getPromotionDiscount() / 100);
                }
                else {
                    discountProduct.setDiscountPrice(discountProduct.getSellPrice());
                    discountProduct.setPromotionDiscount(0);
                }

                list.add(discountProduct);
            }

            return list;
        }
        catch (Exception e){
            System.out.println(String.format("CategoryController findListProductOfCategory ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }

    @PostMapping("/categories")
    public ResponseEntity insertCategory(@Valid @RequestBody Category category){

        try{
            category.setCategoryID(0);
            category.setUpdDate(new Timestamp(System.currentTimeMillis()));
            Category res = repo.save(category);

            if (res == null)
                throw new Exception();

            CATEGORY_CACHE.put(res.getCategoryID(),res);

            return new ResponseEntity(res,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("CategoryController insertCategory ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity updateCategory(@PathVariable(value = "id") Integer id,
                                        @Valid @RequestBody Category category){
        try{
            Optional<Category> old = repo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            old.get().copyFieldValues(category);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));
            Category updatedCategory = repo.save(old.get());

            if (updatedCategory == null)
                throw new Exception();

            CATEGORY_CACHE.replace(id,updatedCategory);

            return new ResponseEntity(updatedCategory,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("CategoryController updateCategory ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity deleteCategory(@PathVariable(value = "id") Integer id){

        try{
            Optional<Category> old = repo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            repo.delete(old.get());

            CATEGORY_CACHE.remove(id);
            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("CategoryController deleteCategory ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
