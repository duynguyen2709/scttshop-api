package com.scttshop.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scttshop.api.Cache.CacheFactoryManager;
import com.scttshop.api.Controller.*;
import com.scttshop.api.Entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableCaching
public class ScttshopApiApplication implements CommandLineRunner {

    @Autowired
    private CustomerController customerController;

    @Autowired
    private UserAccountController userAccountController;

    @Autowired
    private ProductController productController;

    @Autowired
    private CategoryController categoryController;

    @Autowired
    private PromotionController promotionController;

    @Autowired
    private CommentController commentController;

    @Autowired
    private OrderController orderController;

    public static void main(String[] args) {

        SpringApplication.run(ScttshopApiApplication.class, args);

        System.out.println("########## Application started ! ##########");
    }

    @Override public void run(String... args) throws Exception {

        try {
            //Promotion Cache Init
            new Thread(() -> {
                CacheFactoryManager.PROMOTION_CACHE =
                        new ConcurrentHashMap<>(promotionController.getListPromotion().parallelStream().collect(Collectors.toMap(Promotion::getPromotionID, c -> c)));

            }).start();


            //Category Cache Init
            new Thread(() -> {
                CacheFactoryManager.CATEGORY_CACHE =
                        new ConcurrentHashMap<>(categoryController.getListCategories().parallelStream().collect(Collectors.toMap(Category::getCategoryID, c -> c)));

            }).start();

            //Customer Cache Init
            new Thread(() -> {
                CacheFactoryManager.CUSTOMER_CACHE =
                        new ConcurrentHashMap<>(customerController.getListCustomer().parallelStream().collect(Collectors.toMap(Customer::getEmail, c -> c)));
            }).start();

            //UserAccount Cache Init
            new Thread(() -> {
                CacheFactoryManager.USER_ACCOUNT_CACHE =
                        new ConcurrentHashMap<>(userAccountController.getListUserAccount(null).parallelStream().collect(Collectors.toMap(UserAccount::getUsername, c -> c)));
            }).start();

            //Product Cache Init
            new Thread(() -> {
                CacheFactoryManager.PRODUCT_CACHE =
                        new ConcurrentHashMap<>(productController.getListProduct(null).parallelStream().collect(Collectors.toMap(Product::getProductID, c -> c)));

            }).start();


            //Comment Cache Init
            new Thread(() -> {
                CacheFactoryManager.COMMENT_CACHE =
                        new ConcurrentHashMap<>(commentController.getListComment().parallelStream().collect(Collectors.toMap(Comment::getCommentID, c -> c)));
            }).start();

            //OrderLog Cache Init
            new Thread(() -> {
                CacheFactoryManager.ORDER_LOG_CACHE =
                        new ConcurrentHashMap<>(orderController.findAll().parallelStream().collect(Collectors.toMap(Order::getOrderID, c -> c)));
            }).start();

        }
        catch (Exception e) {
            System.out.println("Init Cache ex: " + e.getMessage());
        }
    }

}
