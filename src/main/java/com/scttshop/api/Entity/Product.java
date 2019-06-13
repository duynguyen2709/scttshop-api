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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.scttshop.api.Cache.CacheFactoryManager.COMMENT_CACHE;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoryID", insertable=false, updatable=false, nullable = false)
    @JsonIgnore
    protected Category category;

    @Column(name="categoryID")
    protected int categoryID;

    @Transient
    protected String categoryName;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subCategoryID", insertable=false, updatable=false, nullable = false)
    @JsonIgnore
    protected SubCategory subCategory;

    @Column(name="subCategoryID")
    protected Integer subCategoryID;

    @Transient
    protected String subCategoryName;

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

    @Column(name="viewCount")
    protected int viewCount = 0;

    @Column(name="quantity")
    protected int quantity;

    @Column(name="isActive")
    protected int isActive;

    @Column(name="updDate")
    @JsonIgnore
    protected Timestamp updDate;

//    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL,
//               mappedBy = "product", orphanRemoval = true)
    @Transient
    protected List<Comment> comments = new ArrayList<>();

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }

    public List<Comment> getComments(){


        while (COMMENT_CACHE == null) {
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException e) {
                return Collections.emptyList();
            }
        }

        return COMMENT_CACHE.values().stream().filter(c -> c.getProductID() == this.productID).collect(Collectors.toList());
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

    public static Product ReverseClone(DiscountProduct product){
        Product res = new Product();

        res.productName = product.productName;
        res.categoryID = product.categoryID;
        res.subCategoryID = product.subCategoryID;
        res.manufacturer = product.manufacturer;
        res.image = product.image;
        res.description = product.description;
        res.importPrice = product.importPrice;
        res.sellPrice = product.sellPrice;
        res.viewCount = product.viewCount;
        res.isActive = product.isActive;
        res.quantity = product.quantity;

        return res;
    }
}
