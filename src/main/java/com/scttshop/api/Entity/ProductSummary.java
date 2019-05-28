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
@Entity(name="ProductSummary")
public class ProductSummary implements Serializable {

    @Id
    @JsonIgnore
    private int productID;

    @Column
    private int viewCount = 0;

    @Column
    private int buyCount = 0;

    @Column
    private float averageReview = 0.0f;
//
//    @OneToOne
//    private Product product;
}
