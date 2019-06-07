package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="SubCategory")
public class SubCategory implements Serializable {

    @Id
    @JsonIgnore
    private int subCategoryID;

    @Column
    @JsonIgnore
    private int categoryID;

    @Column
    private String subCategoryName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoryID", insertable=false, updatable=false,nullable = false)
    @JsonIgnore
    private Category category;
}