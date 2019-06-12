package com.scttshop.api.Controller;

import com.scttshop.api.Entity.*;
import com.scttshop.api.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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

import static com.scttshop.api.Cache.CacheFactoryManager.*;

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
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private EntityManager em;

    @GetMapping("/products")
    public List<DiscountProduct> getListProduct(@RequestParam(required = false,defaultValue = "true") Boolean isActive){

        try {
            if (PRODUCT_CACHE != null){

                List<DiscountProduct> collect = PRODUCT_CACHE.values().parallelStream().peek(c -> {
                    c.setRelatedProducts(Collections.emptyList());
                    setPromotion(c);
                }).collect(Collectors.toList());

                if (isActive != null && isActive)
                    collect = collect.stream().filter(c->c.getIsActive() == 1).collect(Collectors.toList());

                return collect;

            }

            List<Product> all = repo.findAll();

            List<DiscountProduct> list = new ArrayList<>();

            for (Product prod : all) {
                DiscountProduct discountProduct = new DiscountProduct(prod);
                discountProduct.setCategoryName(discountProduct.getCategory().getCategoryName());

                if (discountProduct.getSubCategoryID() != null){
                    discountProduct.setSubCategoryName(subCategoryRepository.findById(discountProduct.getSubCategoryID()).get().getSubCategoryName());
                }

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
                    return new ResponseEntity(new EmptyJsonResponse(), HttpStatus.OK);
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
                setPromotion(discountProduct);

                return new ResponseEntity(discountProduct,HttpStatus.OK);
            }

            Optional<Product> product = repo.findById(id);

            if (product.isPresent()) {
                DiscountProduct discountProduct = new DiscountProduct(product.get());
                discountProduct.setCategoryName(discountProduct.getCategory().getCategoryName());
                if (discountProduct.getSubCategoryID() != null){
                    discountProduct.setSubCategoryName(subCategoryRepository.findById(discountProduct.getSubCategoryID()).get().getSubCategoryName());
                }

                setPromotion(discountProduct);

                String query = String.format("SELECT * FROM Product p WHERE p.manufacturer = '%s' " +
                        "AND p.categoryID = '%s' AND p.productID <> '%s' LIMIT 4", discountProduct.getManufacturer(), discountProduct.getCategoryID(), discountProduct.getProductID());

                List<Product>         listProduct = em.createNativeQuery(query, Product.class).getResultList();
                List<DiscountProduct> list        = convertListProduct(listProduct);

                discountProduct.setRelatedProducts(list);
                discountProduct.setComments(COMMENT_CACHE.values().stream().filter(c -> c.getProductID() == id).collect(Collectors.toList()));

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
                if (entity.getSubCategoryID() != null){
                    entity.setSubCategoryName(subCategoryRepository.findById(entity.getSubCategoryID()).get().getSubCategoryName());
                }

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

            while (PROMOTION_CACHE == null)
                Thread.sleep(10);

            for (Promotion promo : PROMOTION_CACHE.values()){
                if (promo.getType().equals("PRODUCT") && promo.getAppliedID() == id && promo.getIsActive() == 1)
                    return promo;
            }

            return null;
            //return promotionRepository.findByTypeAndAppliedIDAndIsActive("PRODUCT", id, 1);
        }
        catch (Exception e){
            System.out.println(String.format("ProductController isOnPromotion ex: %s" , e.getMessage()));
            return null;
        }
    }


    @PostMapping("/products")
    @Transactional
    public ResponseEntity insertProduct(@Valid @RequestBody Product product){

        try{
            product.setProductID(0);
            product.setUpdDate(new Timestamp(System.currentTimeMillis()));
            Product res = repo.save(product);

            if (res == null)
                throw new Exception();

            DiscountProduct discountProduct = new DiscountProduct(res);
            if (discountProduct.getCategoryName() == null || discountProduct.getCategoryName().isEmpty()){
                discountProduct.setCategoryName(CATEGORY_CACHE.get(discountProduct.getCategoryID()).getCategoryName());
            }

            if (discountProduct.getSubCategoryID() != null){
                discountProduct.setSubCategoryName(subCategoryRepository.findById(discountProduct.getSubCategoryID()).get().getSubCategoryName());
            }

            PRODUCT_CACHE.put(res.getProductID(),discountProduct);

            Optional<Category> category = categoryRepository.findById(res.getCategoryID());
            if (!category.isPresent())
                throw new Exception("Category Not Found");

            category.get().setTotalProductType(category.get().getTotalProductType() + 1);
            categoryRepository.save(category.get());

            CATEGORY_CACHE.replace(category.get().getCategoryID(),category.get());

            return new ResponseEntity(discountProduct,HttpStatus.OK);
        }
        catch (Exception e){
            System.out.println(String.format("ProductController insertProduct ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/products/{id}")
    @Transactional
    public ResponseEntity updateProduct(@PathVariable(value = "id") Integer id,
                                        @Valid @RequestBody Product product){
        try{
            Optional<Product> old = repo.findById(id);

            if (!old.isPresent())
                throw new Exception("Product Not Found");

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

            DiscountProduct discountProduct = new DiscountProduct(updatedProduct);
            if (discountProduct.getCategoryName() == null || discountProduct.getCategoryName().isEmpty()){
                discountProduct.setCategoryName(CATEGORY_CACHE.get(discountProduct.getCategoryID()).getCategoryName());
            }

            if (discountProduct.getSubCategoryID() != null){
                discountProduct.setSubCategoryName(subCategoryRepository.findById(discountProduct.getSubCategoryID()).get().getSubCategoryName());
            }

            PRODUCT_CACHE.replace(id,discountProduct);
            return new ResponseEntity(discountProduct,HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("ProductController updateProduct ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/products/{id}/view")
    @Transactional
    public ResponseEntity viewProduct(@PathVariable(value = "id") Integer id){
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

            DiscountProduct discountProduct = new DiscountProduct(updatedProduct);
            if (discountProduct.getCategoryName() == null || discountProduct.getCategoryName().isEmpty()){
                discountProduct.setCategoryName(CATEGORY_CACHE.get(discountProduct.getCategoryID()).getCategoryName());
            }

            if (discountProduct.getSubCategoryID() != null){
                discountProduct.setSubCategoryName(subCategoryRepository.findById(discountProduct.getSubCategoryID()).get().getSubCategoryName());
            }

            PRODUCT_CACHE.replace(id,discountProduct);
            return new ResponseEntity(discountProduct,HttpStatus.OK);

        }
        catch (Exception e){
            System.out.println(String.format("ProductController updateProduct ex: %s" , e.getMessage()));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/products/{id}")
    @Transactional
    public ResponseEntity deleteProduct(@PathVariable(value = "id") Integer id){

        try{
            Optional<Product> old = repo.findById(id);

            if (!old.isPresent()) {
                throw new Exception("Product Not Found");
            }

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
