package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountProduct extends Product {

    private int promotionDiscount;

    private long discountPrice;

    private List<DiscountProduct> relatedProducts;


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
            this.updDate = p.updDate;
            this.categoryID = p.categoryID;
            this.category = p.category;
            this.summary = p.summary;
            this.comments = p.comments;
        }
        catch (NullPointerException e) {
        }
    }
}
