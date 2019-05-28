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
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="Category")
public class Category implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int categoryID;

    @Column(name="categoryName")
    private String categoryName;

    @Column(name="totalProductType")
    private int totalProductType;

    @Column(name="updDate")
    @JsonIgnore
    private Timestamp updDate;

    @OneToMany(cascade = CascadeType.ALL,
               mappedBy = "category", orphanRemoval = true)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY,
               mappedBy = "category", orphanRemoval = true)
    private List<SubCategory> subCategories = new ArrayList<>();

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }

    public void copyFieldValues(Category category) {
        this.categoryName = category.categoryName;
    }
}
