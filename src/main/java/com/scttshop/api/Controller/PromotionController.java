package com.scttshop.api.Controller;

import com.scttshop.api.Entity.DiscountProduct;
import com.scttshop.api.Entity.EmptyJsonResponse;
import com.scttshop.api.Entity.Product;
import com.scttshop.api.Entity.Promotion;
import com.scttshop.api.Repository.ProductRepository;
import com.scttshop.api.Repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
public class PromotionController {

    @Autowired
    private PromotionRepository promotionRepo;

    @Autowired
    private ProductRepository prodRepo;

    @Autowired
    private EntityManager em;

    @GetMapping("/promotions")
    @Cacheable(value="promotions",key="all")
    List<Promotion> getListPromotion() {

        final List<Promotion> all = promotionRepo.findAll();
        for (Promotion promotion: all){

            switch (promotion.getType()) {
                case "PRODUCT":
//                    String query = "SELECT p.productName from Product p JOIN Promotion s " +
//                            "ON p.productID = s.appliedID WHERE s.type = 'PRODUCT' " +
//                            "AND p.productID = " + promotion.getAppliedID();
//
//                    String productName = String.valueOf(em.createNativeQuery(query).getSingleResult());
                    String productName = promotionRepo.getAppliedName(promotion.getAppliedID());
                    promotion.setAppliedName(productName);
                    break;
                case "CATEGORY":
                    break;
                case "SUBCATEGORY":
                    break;
            }
        }

        return all;
    }

    @GetMapping("/promotions/{id}")
    @Cacheable(value="promotions",key="#id")
    ResponseEntity findById(@PathVariable("id") Integer id) {
        Optional<Promotion> promotion = promotionRepo.findById(id);

        if (promotion.isPresent()) {
            return new ResponseEntity(promotion, HttpStatus.OK);
        }

        return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.NOT_FOUND);

    }

    @GetMapping("/promotions/products")
    @Cacheable(value="promotions",key="product")
    List<DiscountProduct> findListProductOnPromotion() {

//        String query = "SELECT p.*,s.promotionDiscount,ROUND(p.sellPrice - p.sellPrice*s.promotionDiscount/100) as discountPrice from Product p JOIN Promotion s ON s.appliedID=p.productID WHERE s.type='PRODUCT' AND s.isActive=1";
//        List<DiscountProduct> product = em.createNativeQuery(query,DiscountProduct.class).getResultList();

//         find with JPA
//         multiple queries
        List<Promotion> promotion = promotionRepo.findByTypeAndIsActiveOrderByAppliedID("PRODUCT",true);

        if (promotion.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<DiscountProduct> product = new ArrayList<>();

        for (Promotion promo : promotion) {
            Optional<Product> prod = prodRepo.findById(promo.getAppliedID());

            if (prod.isPresent()) {
                DiscountProduct discountProduct = new DiscountProduct(prod.get());
                discountProduct.setPromotionDiscount(promo.getPromotionDiscount());
                long newPrice = discountProduct.getSellPrice() -
                        discountProduct.getPromotionDiscount() * discountProduct.getSellPrice() / 100;
                discountProduct.setDiscountPrice(newPrice);

                product.add(discountProduct);
            }
        }

        return product;
    }

}
