package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name="categoryID")
    protected int categoryID;

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

    @Column(name="isActive")
    protected int isActive;

    @Column(name="quantity")
    protected int quantity;

    @Column(name="updDate")
    @JsonIgnore
    protected Timestamp updDate;

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }



}
