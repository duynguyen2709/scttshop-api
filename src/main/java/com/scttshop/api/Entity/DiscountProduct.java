package com.scttshop.api.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountProduct extends Product {

    private int promotionDiscount;

    private long discountPrice;

    public DiscountProduct(Product p){
        this.productID = p.productID;
        this.productName = p.productName;
        this.categoryID = p.categoryID;
        this.manufacturer = p.manufacturer;
        this.image = p.image;
        this.description = p.description;
        this.importPrice = p.importPrice;
        this.sellPrice = p.sellPrice;
        this.isActive = p.isActive;
        this.quantity = p.quantity;
        this.updDate = p.updDate;
    }
}
