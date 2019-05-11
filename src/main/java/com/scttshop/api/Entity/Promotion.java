package com.scttshop.api.Entity;

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

    @Column
    private int promotionDiscount;

    @Column
    private String promotionName;

    @Column
    private Timestamp timeFrom;

    @Column
    private Timestamp timeTo;

    @Column
    private boolean isActive;

    @Column
    private Timestamp updDate;

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }

    public String getTimeFrom(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timeFrom.toLocalDateTime().format(formatter);
    }

    public String getTimeTo(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timeTo.toLocalDateTime().format(formatter);
    }
}
