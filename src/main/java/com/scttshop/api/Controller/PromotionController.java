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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.scttshop.api.Cache.CacheFactoryManager.*;

@RestController
public class PromotionController {

    @Autowired
    private PromotionRepository promotionRepo;

    @Autowired
    private ProductRepository prodRepo;

    @Autowired
    private EntityManager em;

    @GetMapping("/promotions")
    //@Cacheable(value="promotions",key="'all'")
    public List<Promotion> getListPromotion() {

        try {
            if (PROMOTION_CACHE != null){
                return new ArrayList<>(PROMOTION_CACHE.values());
            }

            final List<Promotion> all = promotionRepo.findAll();
            for (Promotion promotion : all) {

                switch (promotion.getType()) {

                    case "PRODUCT":
                        try {
                            String productName = promotionRepo.getAppliedName(promotion.getAppliedID());
                            promotion.setAppliedName(productName);
                        }
                        catch (Exception e) {
                            promotion.setAppliedName("");
                        }
                        break;

                    case "CATEGORY":
                        break;
                }
            }

            return all;
        }
        catch (Exception e){
            System.out.println(String.format("PromotionController getListProduct ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }

    @GetMapping("/promotions/{id}")
    ResponseEntity findById(@PathVariable("id") Integer id) {
        try {

            if (PROMOTION_CACHE!= null)
                return new ResponseEntity(PROMOTION_CACHE.get(id),HttpStatus.OK);

            Optional<Promotion> promotion = promotionRepo.findById(id);

            if (promotion.isPresent()) {
                switch (promotion.get().getType()) {

                    case "PRODUCT":
                        try {
                            String productName = promotionRepo.getAppliedName(promotion.get().getAppliedID());
                            promotion.get().setAppliedName(productName);
                        }
                        catch (Exception e) {
                            promotion.get().setAppliedName("");
                        }
                        break;

                    case "CATEGORY":
                        break;
                }
                PROMOTION_CACHE.putIfAbsent(id,promotion.get());
                return new ResponseEntity(promotion.get(), HttpStatus.OK);
            }

            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);

        }catch (Exception e){
            System.out.println(String.format("PromotionController findById ex: %s" , e.getMessage()));
            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/promotions/products")
    List<DiscountProduct> findListProductOnPromotion() {

        try {
            if (PROMOTION_CACHE != null && PRODUCT_CACHE != null){
                List<Promotion> listPromotionOfProducts = new ArrayList<>(PROMOTION_CACHE.values());
                listPromotionOfProducts.removeIf(c->(!c.getType().equals("PRODUCT") || c.getIsActive() == 0));

                if (listPromotionOfProducts.isEmpty()) {
                    return Collections.EMPTY_LIST;
                }

                List<DiscountProduct> productList = new ArrayList<>();

                for (Promotion promotion : listPromotionOfProducts){
                    productList.add(PRODUCT_CACHE.get(promotion.getAppliedID()));
                }

                return productList;

            }

            List<Promotion> promotion = promotionRepo.findByTypeAndIsActiveOrderByAppliedID("PRODUCT", 1);

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
        catch (Exception e){
            System.out.println(String.format("PromotionController findListProductOnPromotion ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }


    @PostMapping("/promotions")
    public ResponseEntity insertPromotion(@Valid @RequestBody Promotion promotion){

        try{
            promotion.setPromotionID(0);
            promotion.setUpdDate(new Timestamp(System.currentTimeMillis()));
            Promotion res = promotionRepo.save(promotion);

            if (res == null)
                throw new Exception();

            res.setAppliedName(promotionRepo.getAppliedName(res.getAppliedID()));
            PROMOTION_CACHE.put(res.getPromotionID(),res);


            return new ResponseEntity(res,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("PromotionController insertPromotion ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/promotions/{id}")
    public ResponseEntity updatePromotion(@PathVariable(value = "id") Integer id,
                                          @Valid @RequestBody Promotion promotion){
        try{
            Optional<Promotion> old = promotionRepo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            old.get().copyFieldValues(promotion);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));

            Promotion updatedPromotion = promotionRepo.save(old.get());
            if (updatedPromotion == null)
                throw new Exception();

            updatedPromotion.setAppliedName(promotionRepo.getAppliedName(updatedPromotion.getAppliedID()));
            PROMOTION_CACHE.replace(id,updatedPromotion);

            return new ResponseEntity(updatedPromotion,HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("PromotionController updatePromotion ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/promotions/{id}")
    public ResponseEntity deletePromotion(@PathVariable(value = "id") Integer id){

        try{
            Optional<Promotion> old = promotionRepo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            promotionRepo.delete(old.get());

            PROMOTION_CACHE.remove(id);

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("PromotionController deletePromotion ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
