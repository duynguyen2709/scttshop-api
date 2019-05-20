package com.scttshop.api.Controller;

import com.scttshop.api.Entity.DiscountProduct;
import com.scttshop.api.Entity.EmptyJsonResponse;
import com.scttshop.api.Entity.Product;
import com.scttshop.api.Entity.Promotion;
import com.scttshop.api.Repository.ProductRepository;
import com.scttshop.api.Repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.Valid;
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
    @Cacheable(value="promotions",key="'all'")
    List<Promotion> getListPromotion() {

        final List<Promotion> all = promotionRepo.findAll();
        for (Promotion promotion: all){

            switch (promotion.getType()) {

                case "PRODUCT":
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
    @Cacheable(value="promotions",key="'product'")
    List<DiscountProduct> findListProductOnPromotion() {

//        String query = "SELECT p.*,s.promotionDiscount,ROUND(p.sellPrice - p.sellPrice*s.promotionDiscount/100) as discountPrice from Product p JOIN Promotion s ON s.appliedID=p.productID WHERE s.type='PRODUCT' AND s.isActive=1";
//        List<DiscountProduct> product = em.createNativeQuery(query,DiscountProduct.class).getResultList();

//         find with JPA
//         multiple queries
        List<Promotion> promotion = promotionRepo.findByTypeAndIsActiveOrderByAppliedID("PRODUCT",1);

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


    @PostMapping("/promotions")
    @Caching(
            put= { @CachePut(value= "promotions", key= "#promotion.promotionID") },
            evict= { @CacheEvict(value= "promotions", key="'all'"),
                     @CacheEvict(value= "promotions", key="'product'")}
    )
    public ResponseEntity insertPromotion(@Valid @RequestBody Promotion promotion){

        try{
            promotion.setPromotionID(0);
            Promotion res = promotionRepo.save(promotion);

            if (res == null)
                throw new Exception();

            return new ResponseEntity(res,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/promotions/{id}")
    @Caching(
            put= { @CachePut(value= "promotions", key= "#id") },
            evict= { @CacheEvict(value= "promotions", key="'all'"),
                     @CacheEvict(value= "promotions", key="'product'")}
    )
    public ResponseEntity updatePromotion(@PathVariable(value = "id") Integer id,
                                          @Valid @RequestBody Promotion promotion){
        try{
            Optional<Promotion> old = promotionRepo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            old.get().copyFieldValues(promotion);

            Promotion updatedPromotion = promotionRepo.save(old.get());

            if (updatedPromotion == null)
                throw new Exception();

            return new ResponseEntity(updatedPromotion,HttpStatus.OK);

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

    @DeleteMapping("/promotions/{id}")
    @Caching(
            evict= {
                    @CacheEvict(value="promotions",key="#id"),
                    @CacheEvict(value= "promotions", key="'all'"),
                    @CacheEvict(value= "promotions", key="'product'")
            }
    )
    public ResponseEntity deletePromotion(@PathVariable(value = "id") Integer id){

        try{
            Optional<Promotion> old = promotionRepo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            promotionRepo.delete(old.get());

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
