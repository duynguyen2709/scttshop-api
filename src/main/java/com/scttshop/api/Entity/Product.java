package com.scttshop.api.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="Product")
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int productID;

    @Column(name="productName")
    private String productName;

    @Column(name="categoryID")
    private int categoryID;

    @Column(name="manufacturer")
    private String manufacturer;

    @Column(name="image")
    private String image;

    @Column(name="description")
    private String description;

    @Column(name="importPrice")
    private long importPrice;

    @Column(name="sellPrice")
    private long sellPrice;

    @Column(name="isActive")
    private int isActive;

    @Column(name="quantity")
    private int quantity;

    @Column(name="updDate")
    private Timestamp updDate;

    public LocalDateTime getUpdDate(){

        return updDate.toLocalDateTime();
    }



}
