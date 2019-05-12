package com.scttshop.api.Controller;

import com.scttshop.api.Entity.*;
import com.scttshop.api.Repository.CategoryRepository;
import com.scttshop.api.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
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
    @Cacheable("categories")
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
}
