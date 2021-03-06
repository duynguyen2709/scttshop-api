package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@Entity(name="Promotion")
public class Promotion implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int promotionID;

    @Column
    private String type;

    @Column
    private int appliedID;

    @Transient
    private String appliedName;

    @Column
    private int promotionDiscount;

    @Column
    private String promotionName;

    @Column
    private int isActive;

    @Column
    @JsonIgnore
    private Timestamp updDate;

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }



    public void copyFieldValues(Promotion promotion) {
        this.type = promotion.type;
        this.appliedID = promotion.appliedID;
        this.promotionDiscount = promotion.promotionDiscount;
        this.promotionName = promotion.promotionName;
        this.isActive = promotion.isActive;
    }
}
