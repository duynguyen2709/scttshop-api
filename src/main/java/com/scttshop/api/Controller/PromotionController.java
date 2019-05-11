package com.scttshop.api.Controller;

import com.scttshop.api.Entity.EmptyJsonResponse;
import com.scttshop.api.Entity.Product;
import com.scttshop.api.Entity.Promotion;
import com.scttshop.api.Repository.ProductRepository;
import com.scttshop.api.Repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
public class PromotionController  {

    @Autowired
    private PromotionRepository promotionRepo;

    @Autowired
    private ProductRepository prodRepo;

    @Autowired
    private EntityManager em;

    @GetMapping("/promotions")
    List<Promotion> getListPromotion(){

        return promotionRepo.findAll();
    }

    @GetMapping("/promotions/{id}")
    ResponseEntity findById(@PathVariable("id") Integer id) {
        Optional<Promotion> promotion = promotionRepo.findById(id);

        if (promotion.isPresent())
            return new ResponseEntity(promotion, HttpStatus.OK);

        return new ResponseEntity(new EmptyJsonResponse(),HttpStatus.NOT_FOUND);

    }

    @GetMapping("/promotions/products")
    List<Product> findListProductOnPromotion(){
        String query = "SELECT * FROM Product p JOIN Promotion s ON p.productID = s.appliedID WHERE s" +
                ".type='PRODUCT' AND s.isActive=1";

        return (List<Product>) em.createNativeQuery(query, Product.class).getResultList();
    }

}
