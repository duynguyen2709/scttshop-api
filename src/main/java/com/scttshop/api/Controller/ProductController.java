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
import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class ProductController {

    @Autowired
    private ProductRepository repo;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    EntityManager em;

    @GetMapping("/products")
    @Cacheable(value = "products")
    public List<DiscountProduct> findAll(){

//        final List<Product> all = em.createQuery("SELECT p FROM Product p",Product.class).getResultList();

        List<Product> all = repo.findAll();

        List<DiscountProduct> list = new ArrayList<>();

        for (Product prod: all){
            DiscountProduct discountProduct = new DiscountProduct(prod);

            setPromotion(discountProduct);

            list.add(discountProduct);
        }

        return list;
    }

    @GetMapping("/products/{id}")
    @Cacheable(value = "products",key="#id")
    public ResponseEntity findById(@PathVariable("id") Integer id) {
        Optional<Product> product = repo.findById(id);

        if (product.isPresent()) {
            DiscountProduct discountProduct = new DiscountProduct(product.get());

            setPromotion(discountProduct);

            String query = String.format("SELECT * FROM Product p WHERE p.manufacturer = '%s' " +
                            "AND p.categoryID = '%s' AND p.productID <> '%s' LIMIT 4"
                    ,discountProduct.getManufacturer()
                    ,discountProduct.getCategoryID()
                    ,discountProduct.getProductID());

            List<Product> listProduct = em.createNativeQuery(query,Product.class).getResultList();
            List<DiscountProduct> list = convertListProduct(listProduct);

            discountProduct.setRelatedProducts(list);

            return new ResponseEntity(discountProduct, HttpStatus.OK);
        }

        return new ResponseEntity(new EmptyJsonResponse(),HttpStatus.NOT_FOUND);

    }

    private List<DiscountProduct> convertListProduct(List<Product> listProduct){
        List<DiscountProduct> result = new ArrayList<>();

        for (Product prod: listProduct){
            DiscountProduct entity = new DiscountProduct(prod);

            setPromotion(entity);

            result.add(entity);
        }

        return result;

    }

    private void setPromotion(DiscountProduct discountProduct) {
        Promotion promotion = isOnPromotion(discountProduct.getProductID());

        if (promotion != null) {
            discountProduct.setPromotionDiscount(promotion.getPromotionDiscount());
            discountProduct.setDiscountPrice(discountProduct.getSellPrice() -
                    discountProduct.getSellPrice() * discountProduct.getPromotionDiscount() / 100);
        }
        else {
            discountProduct.setDiscountPrice(discountProduct.getSellPrice());
            discountProduct.setPromotionDiscount(0);
        }
    }

    @Cacheable(value="promotions",key="'product' + #id")
    public Promotion isOnPromotion(Integer id){
        Promotion promo = promotionRepository.findByTypeAndAppliedIDAndIsActive("PRODUCT",id,1);

        return promo;
    }
}
