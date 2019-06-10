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
import java.util.*;
import java.util.stream.Collectors;

import static com.scttshop.api.Cache.CacheFactoryManager.CATEGORY_CACHE;
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
    public List<DiscountProduct> getListProduct(){

        try {
            if (PRODUCT_CACHE != null){

                return PRODUCT_CACHE.values().parallelStream().peek(c -> c.setRelatedProducts(Collections.emptyList())).collect(Collectors.toList());

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

    @GetMapping("/products/{id}")
    public ResponseEntity findById(@PathVariable("id") Integer id) {
        try {

            if (PRODUCT_CACHE != null) {
                DiscountProduct discountProduct = PRODUCT_CACHE.get(id);

                if (discountProduct == null){
                    new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);
                }

                String query = String.format("SELECT p.productID FROM Product p WHERE p.manufacturer = '%s' " +
                        "AND p.categoryID = '%s' AND p.productID <> '%s' LIMIT 4", discountProduct.getManufacturer(), discountProduct.getCategoryID(), discountProduct.getProductID());

                List<Integer> listProductID = em.createNativeQuery(query).getResultList();

                List<DiscountProduct> relatedProducts =  listProductID.stream()
                                                        .map(c -> {
                                                            DiscountProduct entity = PRODUCT_CACHE.get(c);
                                                            entity.setRelatedProducts(Collections.emptyList());
                                                            return entity;
                                                        })
                                                        .collect(Collectors.toList());

                discountProduct.setRelatedProducts(relatedProducts);
                discountProduct.setComments(PRODUCT_CACHE.get(id).getComments());

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
                PRODUCT_CACHE.putIfAbsent(discountProduct.getProductID(),discountProduct);
                return new ResponseEntity(discountProduct, HttpStatus.OK);
            }

            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("ProductController findById ex: %s" , e.getMessage()));
            return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.BAD_REQUEST);
        }

    }

    private List<DiscountProduct> convertListProduct(List<Product> listProduct){

        List<DiscountProduct> result = new ArrayList<>();

        for (Product prod: listProduct){
            try {
                DiscountProduct entity = new DiscountProduct(prod);
                entity.setCategoryName(entity.getCategory().getCategoryName());
                entity.setRelatedProducts(Collections.emptyList());
                setPromotion(entity);

                result.add(entity);
            }
            catch (Exception e){
                System.out.println(String.format("ProductController convertListProduct ex: %s" , e.getMessage()));
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

    public Promotion isOnPromotion(Integer id){
        try {
            Promotion promo = promotionRepository.findByTypeAndAppliedIDAndIsActive("PRODUCT", id, 1);

            return promo;
        }
        catch (Exception e){
            System.out.println(String.format("ProductController isOnPromotion ex: %s" , e.getMessage()));
            return null;
        }
    }


    @PostMapping("/products")
    public ResponseEntity insertProduct(@Valid @RequestBody Product product){

        try{
            product.setProductID(0);
            product.setUpdDate(new Timestamp(System.currentTimeMillis()));
            Product res = repo.save(product);

            if (res == null)
                throw new Exception();

            PRODUCT_CACHE.put(res.getProductID(),new DiscountProduct(res));

            Optional<Category> category = categoryRepository.findById(res.getCategoryID());
            if (!category.isPresent())
                throw new Exception("Category Not Found");

            category.get().setTotalProductType(category.get().getTotalProductType() + 1);
            categoryRepository.save(category.get());

            CATEGORY_CACHE.replace(category.get().getCategoryID(),category.get());

            return new ResponseEntity(res,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("ProductController insertProduct ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity updateProduct(@PathVariable(value = "id") Integer id,
                                        @Valid @RequestBody Product product){
        try{
            Optional<Product> old = repo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            if (old.get().getCategoryID() != product.getCategoryID()){
                Optional<Category> category = categoryRepository.findById(product.getCategoryID());
                if (!category.isPresent())
                    throw new Exception("Category Not Found");

                category.get().setTotalProductType(category.get().getTotalProductType() + 1);
                categoryRepository.save(category.get());

                CATEGORY_CACHE.replace(category.get().getCategoryID(),category.get());

                category = categoryRepository.findById(old.get().getCategoryID());
                if (!category.isPresent())
                    throw new Exception("Category Not Found");

                category.get().setTotalProductType(category.get().getTotalProductType() - 1);
                categoryRepository.save(category.get());

                CATEGORY_CACHE.replace(category.get().getCategoryID(),category.get());
            }

            old.get().copyFieldValues(product);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));
            Product updatedProduct = repo.save(old.get());

            if (updatedProduct == null)
                throw new Exception();

            PRODUCT_CACHE.replace(id,new DiscountProduct(updatedProduct));
            return new ResponseEntity(new DiscountProduct(updatedProduct),HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("ProductController updateProduct ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/products/{id}/view")
    public ResponseEntity updateProduct(@PathVariable(value = "id") Integer id){
        try{
            Optional<Product> old = repo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            int oldViewCount = old.get().getViewCount();
            old.get().setViewCount(oldViewCount + 1);
            old.get().setUpdDate(new Timestamp(System.currentTimeMillis()));
            Product updatedProduct = repo.save(old.get());

            if (updatedProduct == null)
                throw new Exception();

            PRODUCT_CACHE.replace(id,new DiscountProduct(updatedProduct));
            return new ResponseEntity(new DiscountProduct(updatedProduct),HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("ProductController updateProduct ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity deleteProduct(@PathVariable(value = "id") Integer id){

        try{
            Optional<Product> old = repo.findById(id);

            if (!old.isPresent())
                return ResponseEntity.notFound().build();

            repo.delete(old.get());
            PRODUCT_CACHE.remove(id);

            Optional<Category> category = categoryRepository.findById(old.get().getCategoryID());
            if (!category.isPresent())
                throw new Exception("Category Not Found");

            category.get().setTotalProductType(category.get().getTotalProductType() - 1);
            categoryRepository.save(category.get());

            CATEGORY_CACHE.replace(category.get().getCategoryID(),category.get());

            return new ResponseEntity(HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("ProductController deleteProduct ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/products/search")
    public ResponseEntity searchProduct(@RequestBody ProductSearch product){
        try{

            List<DiscountProduct> result = new ArrayList<>(PRODUCT_CACHE.values());

            if (product.getProductName() != null && !product.getProductName().isEmpty())
                result.removeIf(next -> !next.getProductName().toLowerCase().contains(product.getProductName().toLowerCase()));

            if (product.getIsOnPromotion())
                result.removeIf(next -> next.getPromotionDiscount() == 0);

            if (product.getMinPrice() > 0)
                result.removeIf(next -> next.getDiscountPrice() < product.getMinPrice());

            if (product.getMaxPrice() > 0)
                result.removeIf(next -> next.getDiscountPrice() > product.getMaxPrice());

            if (product.getCategoryID() != 0)
                result.removeIf(next -> next.getCategoryID() != product.getCategoryID());

            return new ResponseEntity(result,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("ProductController searchProduct ex: %s" , e.getMessage()));
            return new ResponseEntity(new EmptyJsonResponse(),HttpStatus.OK);
        }
    }
}
