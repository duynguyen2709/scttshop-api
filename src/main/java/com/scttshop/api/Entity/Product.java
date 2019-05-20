package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="Product")
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    protected int productID;

    @Column(name="productName")
    protected String productName;

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JoinColumn(name = "categoryID", insertable=false, updatable=false,nullable = false)
    @JsonIgnore
    protected Category category;

    @Column(nullable = false)
    protected Integer categoryID;

    @Transient
    protected String categoryName;

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JoinColumn(name = "subCategoryID", insertable=false, updatable=false,nullable = true)
    @JsonIgnore
    protected SubCategory subCategory;

    @Transient
    protected String subCategoryName;

    @Column(nullable = true)
    protected Integer subCategoryID;

    @Column(name="manufacturer")
    protected String manufacturer;

    @Column(name="image")
    protected String image;

    @Column(name="description")
    protected String description;

    @Column(name="importPrice")
    protected long importPrice;

    @Column(name="sellPrice")
    protected long sellPrice;

    @Column(name="quantity")
    protected int quantity;

    @Column(name="isActive")
    protected int isActive;

    @Column(name="updDate")
    @JsonIgnore
    protected Timestamp updDate;

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }

    public void copyFieldValues(Product product) {
        this.productName = product.productName;
        this.categoryID = product.categoryID;
        this.subCategoryID = product.subCategoryID;
        this.manufacturer = product.manufacturer;
        this.image = product.image;
        this.description = product.description;
        this.importPrice = product.importPrice;
        this.sellPrice = product.sellPrice;
        this.isActive = product.isActive;
        this.quantity = product.quantity;

    }
}
