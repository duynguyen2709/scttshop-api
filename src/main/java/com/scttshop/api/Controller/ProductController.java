package com.scttshop.api.Controller;

import com.scttshop.api.Entity.*;
import com.scttshop.api.Repository.CategoryRepository;
import com.scttshop.api.Repository.CommentRepository;
import com.scttshop.api.Repository.ProductRepository;
import com.scttshop.api.Repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

import static com.scttshop.api.Cache.CacheFactoryManager.PRODUCT_CACHE;

@RestController
public class ProductController {

    @Autowired
    private ProductRepository repo;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CommentRepository commentRepo;

    @Autowired
    private EntityManager em;

    @GetMapping("/products")
    //@Cacheable(value = "'products'",key="'all'")
    public List<DiscountProduct> getListProduct(){

        try {
            if (PRODUCT_CACHE != null){
                return new ArrayList<>(PRODUCT_CACHE.values());
            }

            List<Product> all = repo.findAll();

            List<DiscountProduct> list = new ArrayList<>();

            for (Product prod : all) {
                DiscountProduct discountProduct = new DiscountProduct(prod);
                discountProduct.setCategoryName(discountProduct.getCategory().getCategoryName());
                setPromotion(discountProduct);

                list.add(discountProduct);
            }

            return list;
        }
        catch (Exception e){
            System.out.println(String.format("ProductController getListProduct ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }

    @GetMapping("/products/{id}/comments")
    @org.springframework.transaction.annotation.Transactional
    public List<Comment> getListComment(@PathVariable("id") Integer id){

        try{

            Optional<Product> product = repo.findById(id);

            if (!product.isPresent())
                return Collections.EMPTY_LIST;

            return product.get().getComments();
        }
        catch (Exception e){
            System.out.println(String.format("ProductController getListComment ex: %s" , e.getMessage()));
            return Collections.emptyList();
        }
    }

    @GetMapping("/products/{id}")
    //@Cacheable(value = "products",key="#id")
    public ResponseEntity findById(@PathVariable("id") Integer id) {
        try {

            if (PRODUCT_CACHE!= null && PRODUCT_CACHE.contains(id)) {
                DiscountProduct discountProduct = PRODUCT_CACHE.get(id);

                String query = String.format("SELECT p.productID FROM Product p WHERE p.manufacturer = '%s' " +
                        "AND p.categoryID = '%s' AND p.productID <> '%s' LIMIT 4", discountProduct.getManufacturer(), discountProduct.getCategoryID(), discountProduct.getProductID());

                List<Integer> listProductID = em.createNativeQuery(query,Integer.class).getResultList();

                List<DiscountProduct> relatedProducts =  listProductID.stream()
                                                        .map(c -> PRODUCT_CACHE.get(c))
                                                        .collect(Collectors.toList());
                discountProduct.setRelatedProducts(relatedProducts);

                return new ResponseEntity(discountProduct,HttpStatus.OK);
            }

            Optional<Product> product = repo.findById(id);

            if (product.isPresent()) {
                DiscountProduct discountProduct = new DiscountProduct(product.get());
                discountProduct.setCategoryName(discountProduct.getCategory().getCategoryName());
                setPromotion(discountProduct);

                String query = String.format("SELECT * FROM Product p WHERE p.manufacturer = '%s' " +
                        "AND p.categoryID = '%s' AND p.productID <> '%s' LIMIT 4", discountProduct.getManufacturer(), discountProduct.getCategoryID(), discountProduct.getProductID());

                List<Product>         listProduct = em.createNativeQuery(query, Product.class).getResultList();
                List<DiscountProduct> list        = convertListProduct(listProduct);

                discountProduct.setRelatedProducts(list);

                return new ResponseEntity(discountProduct, HttpStatus.OK);
            }

            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("CategoryController findById ex: %s" , e.getMessage()));
            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.BAD_REQUEST);
        }

    }

    private List<DiscountProduct> convertListProduct(List<Product> listProduct){

        List<DiscountProduct> result = new ArrayList<>();

        for (Product prod: listProduct){
            try {
                DiscountProduct entity = new DiscountProduct(prod);
                entity.setCategoryName(entity.getCategory().getCategoryName());
                setPromotion(entity);

                result.add(entity);
            }
            catch (Exception e){
                System.out.println(String.format("CategoryController convertListProduct ex: %s" , e.getMessage()));
            }
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

    //@Cacheable(value="promotions",key="'product' + #id")
    public Promotion isOnPromotion(Integer id){
        try {
            Promotion promo = promotionRepository.findByTypeAndAppliedIDAndIsActive("PRODUCT", id, 1);

            return promo;
        }
        catch (Exception e){
            System.out.println(String.format("CategoryController isOnPromotion ex: %s" , e.getMessage()));
            return null;
        }
    }


    @PostMapping("/products")
    //    @Caching(
    //            put= { @CachePut(value= "products", key= "#product.productID") },
    //            evict= { @CacheEvict(value= "products", key="'all'")}
    //    )
    public ResponseEntity insertProduct(@Valid @RequestBody Product product){

        try{
            product.setProductID(0);
            product.setUpdDate(new Timestamp(System.currentTimeMillis()));
            Product res = repo.save(product);

            if (res == null)
                throw new Exception();

            return new ResponseEntity(res,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("CategoryController insertProduct ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/products/{id}")
    //    @Caching(
    //            put= { @CachePut(value= "products", key= "#id") },
    //            evict= { @CacheEvict(value= "products", key="'all'")}
    //    )
    public ResponseEntity updateProduct(@PathVariable(value = "id") Integer id,
                                        @Valid @RequestBody Product product){
        try{
            Optional<Product> old = repo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            old.get().copyFieldValues(product);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));
            Product updatedProduct = repo.save(old.get());

            if (updatedProduct == null)
                throw new Exception();

            return new ResponseEntity(updatedProduct,HttpStatus.OK);

        }
        //        catch (ChangeSetPersister.NotFoundException e){
        //            System.out.println(e.getMessage());
        //            return new ResponseEntity(new EmptyJsonResponse(),HttpStatus.OK);
        //        }
        catch (Exception e){
            System.out.println(String.format("CategoryController updateProduct ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/products/{id}")
    //    @Caching(
    //            evict= {
    //                    @CacheEvict(value="products",key="#id"),
    //                    @CacheEvict(value= "products", key="'all'")
    //            }
    //    )
    public ResponseEntity deleteProduct(@PathVariable(value = "id") Integer id){

        try{
            Optional<Product> old = repo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            repo.delete(old.get());

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("CategoryController deleteProduct ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
