package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="SubCategory")
public class SubCategory implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    //@JsonIgnore
    private int subCategoryID;

    @Column(name="categoryID")
    @JsonIgnore
    private int categoryID;

    @Column(name="subCategoryName")
    private String subCategoryName;

    @Column(name="totalProductType")
    @JsonIgnore
    private int totalProductType;

    @Column(name="updDate")
    @JsonIgnore
    private Timestamp updDate;

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }

}