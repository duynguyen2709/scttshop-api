package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private long totalPrice;

    @Column
    private String paymentType;

    @Column
    private String status;

    @Column
    private String extraInfo;

    @Column
    @JsonIgnore
    private Timestamp updDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "email", insertable=false, updatable=false, nullable = false)
    @JsonIgnore
    protected Customer customer;

    @Transient
    @JsonIgnore
    private List<OrderDetail> listProduct = new ArrayList<>();

    public List<OrderDetail> getOrderDetail(){
        try {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<List<OrderDetail>> typeRef = new TypeReference<List<OrderDetail>>(){};
            listProduct =  mapper.readValue(orderDetail, typeRef);
        }
        catch (Exception e){
            listProduct =  Collections.EMPTY_LIST;
        }

        return listProduct;
    }

//    public void setOrderDetail(){
//        try{
//            ByteArrayOutputStream out    = new ByteArrayOutputStream();
//            ObjectMapper          mapper = new ObjectMapper();
//
//            mapper.writeValue(out, listProduct);
//
//            final byte[] data = out.toByteArray();
//            orderDetail = new String(data);
//        }
//        catch (Exception e){
//            orderDetail = "";
//        }
//    }

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }

    public String getOrderTime(){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return orderTime.toLocalDateTime().format(formatter);
        }
        catch (Exception e){
            return "";
        }
    }

    public void copyFieldValues(Order order) {
        this.paymentType=order.paymentType;
        this.status = order.status;
        this.extraInfo = order.extraInfo;
    }
}
