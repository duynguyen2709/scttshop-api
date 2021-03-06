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
                            String productName = prodRepo.findById(promotion.getAppliedID()).get().getProductName();
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

            if (PROMOTION_CACHE!= null) {

                Promotion promotion = PROMOTION_CACHE.get(id);

                return promotion == null ? new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK)
                        : new ResponseEntity(promotion, HttpStatus.OK);

            }

            Optional<Promotion> promotion = promotionRepo.findById(id);

            if (promotion.isPresent()) {
                switch (promotion.get().getType()) {

                    case "PRODUCT":
                        try {
                            String productName = prodRepo.findById(promotion.get().getAppliedID()).get().getProductName();
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
                    DiscountProduct prod = new DiscountProduct();
                    prod.clone(PRODUCT_CACHE.get(promotion.getAppliedID()));
                    if (prod.getIsActive() == 1)
                        productList.add(prod);
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
            if (promotion.getPromotionDiscount() <= 0)
                throw new Exception("Promotion Discount Must Be Higher Than 0");

            promotion.setUpdDate(new Timestamp(System.currentTimeMillis()));
            Promotion res = promotionRepo.save(promotion);

            if (res == null)
                throw new Exception();

            res.setAppliedName(prodRepo.findById(promotion.getAppliedID()).get().getProductName());
            PROMOTION_CACHE.put(res.getPromotionID(),res);

            DiscountProduct prod = PRODUCT_CACHE.get(res.getAppliedID());
            prod.setPromotionDiscount(res.getPromotionDiscount());
            prod.setDiscountPrice(prod.getSellPrice() - prod.getSellPrice() * prod.getPromotionDiscount() / 100);

            PRODUCT_CACHE.replace(prod.getProductID(),prod);

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
                throw new Exception("Promotion Not Found");

            DiscountProduct prod = PRODUCT_CACHE.get(old.get().getAppliedID());
            prod.setPromotionDiscount(0);
            prod.setDiscountPrice(prod.getSellPrice());

            PRODUCT_CACHE.replace(prod.getProductID(),prod);

            old.get().copyFieldValues(promotion);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));

            Promotion updatedPromotion = promotionRepo.save(old.get());
            if (updatedPromotion == null)
                throw new Exception("Saving new promotion return null");

            updatedPromotion.setAppliedName(prodRepo.findById(promotion.getAppliedID()).get().getProductName());
            PROMOTION_CACHE.replace(id,updatedPromotion);

            prod = PRODUCT_CACHE.get(updatedPromotion.getAppliedID());
            prod.setPromotionDiscount(updatedPromotion.getPromotionDiscount());
            prod.setDiscountPrice(prod.getSellPrice() - prod.getSellPrice() * prod.getPromotionDiscount() / 100);


            PRODUCT_CACHE.replace(prod.getProductID(),prod);

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

            DiscountProduct prod = PRODUCT_CACHE.get(old.get().getAppliedID());
            prod.setPromotionDiscount(0);
            prod.setDiscountPrice(prod.getSellPrice());


            PRODUCT_CACHE.replace(prod.getProductID(),prod);

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("PromotionController deletePromotion ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
