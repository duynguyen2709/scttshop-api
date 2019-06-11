package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="SubCategory")
public class SubCategory implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer subCategoryID;

    @Column
    private int categoryID;

    @Column
    private String subCategoryName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoryID", insertable=false, updatable=false,nullable = false)
    @JsonIgnore
    private Category category;

    @OneToMany(cascade = CascadeType.ALL,
               mappedBy = "subCategory", orphanRemoval = true)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    public void copyFieldValues(SubCategory subCategory) {
        this.subCategoryName = subCategory.subCategoryName;
    }
}
