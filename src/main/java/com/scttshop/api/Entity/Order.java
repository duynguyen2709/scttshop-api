package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="Order")
@Table(name="OrderLog")
public class Order implements Serializable {

    @Id
    private String orderID;

    @Column
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Timestamp orderTime;

    @Column
    private String email;

    @Column
    private String orderDetail;

    @Column
    private String totalPrice;

    @Column
    private String paymentType;

    @Column
    private String status;

    @Column
    private String extraInfo;

    @Column
    @JsonIgnore
    private Timestamp updDate;

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }

    public String getOrderTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return orderTime.toLocalDateTime().format(formatter);
    }

    public void copyFieldValues(Order order) {
        this.orderDetail = order.orderDetail;
        this.paymentType=order.paymentType;
        this.status = order.status;
        this.extraInfo = order.extraInfo;
    }
}
