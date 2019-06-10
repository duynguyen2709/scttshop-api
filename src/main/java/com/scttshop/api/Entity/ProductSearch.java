package com.scttshop.api.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductSearch implements Serializable {

    private String productName = "";

    private int categoryID = 0;

    private long minPrice = 0;

    private long maxPrice = 0;

    private Boolean isOnPromotion = false;
}
