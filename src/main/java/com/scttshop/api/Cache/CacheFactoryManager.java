package com.scttshop.api.Cache;

import com.scttshop.api.Entity.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author duyna5
 */
public class CacheFactoryManager {

    public static ConcurrentHashMap<String, UserAccount> USER_ACCOUNT_CACHE = null;
    public static ConcurrentHashMap<String, Customer>    CUSTOMER_CACHE     = null;
    public static ConcurrentHashMap<Integer, Comment>    COMMENT_CACHE      = null;
    public static ConcurrentHashMap<Integer, Category>   CATEGORY_CACHE     = null;
    public static ConcurrentHashMap<Integer, DiscountProduct>    PRODUCT_CACHE      = null;
    public static ConcurrentHashMap<Integer, Promotion>  PROMOTION_CACHE    = null;
    public static ConcurrentHashMap<String, Order>  ORDER_LOG_CACHE    = null;

}
