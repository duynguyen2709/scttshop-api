package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Collections;
import java.util.List;

import static com.scttshop.api.Cache.CacheFactoryManager.CATEGORY_CACHE;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountProduct extends Product {

    private int promotionDiscount;

    private long discountPrice;

    private List<DiscountProduct> relatedProducts;

    public DiscountProduct clone(DiscountProduct p){

        try {
            this.productID = p.productID;
            this.productName = p.productName;
            this.manufacturer = p.manufacturer;
            this.image = p.image;
            this.description = p.description;
            this.importPrice = p.importPrice;
            this.sellPrice = p.sellPrice;
            this.isActive = p.isActive;
            this.quantity = p.quantity;
            this.viewCount = p.viewCount;
            this.updDate = p.updDate;
            this.categoryID = p.categoryID;
            while (CATEGORY_CACHE == null)
                Thread.sleep(10);

            this.category = CATEGORY_CACHE.get(p.categoryID);
            if (this.category != null)
                this.categoryName = this.category.getCategoryName();

            this.subCategoryID = p.subCategoryID;
            //this.subCategory = p.subCategory;
            if (this.subCategory != null)
                this.subCategoryName = this.subCategory.getSubCategoryName();

            this.discountPrice = p.discountPrice;
            this.promotionDiscount = p.promotionDiscount;
            this.relatedProducts = Collections.emptyList();
        }
        catch (Exception e) {
        }

        return this;
    }

    public DiscountProduct(Product p) {
        try {
            this.productID = p.productID;
            this.productName = p.productName;
            this.manufacturer = p.manufacturer;
            this.image = p.image;
            this.description = p.description;
            this.importPrice = p.importPrice;
            this.sellPrice = p.sellPrice;
            this.isActive = p.isActive;
            this.quantity = p.quantity;
            this.viewCount = p.viewCount;
            this.updDate = p.updDate;
            this.categoryID = p.categoryID;
            while (CATEGORY_CACHE == null)
                Thread.sleep(10);

            this.category = CATEGORY_CACHE.get(p.categoryID);
            this.comments = p.comments;
            if (this.category != null)
                this.categoryName = this.category.getCategoryName();

            this.subCategoryID = p.subCategoryID;
            //this.subCategory = p.subCategory;
            if (this.subCategory != null)
                this.subCategoryName = this.subCategory.getSubCategoryName();
        }
        catch (Exception e) {
        }
    }
}
