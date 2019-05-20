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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "categoryID", cascade = CascadeType.ALL)
    private List<SubCategory> subCategories = new ArrayList<>();


    @Column(name="updDate")
    @JsonIgnore
    private Timestamp updDate;

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }

    public void copyFieldValues(Category category) {
        this.categoryName = category.categoryName;
    }
}
