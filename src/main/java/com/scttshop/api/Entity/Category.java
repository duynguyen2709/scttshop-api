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
@Entity(name="Category")
public class Category implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int categoryID;

    @Column(name="categoryName")
    private String categoryName;

    @Column(name="categorySubType")
    private String categorySubType;

    @Column(name="updDate")
    private Timestamp updDate;

    public LocalDateTime getUpdDate(){

        return updDate.toLocalDateTime();
    }



}
