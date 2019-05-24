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
@Entity(name="Customer")
public class Customer implements Serializable {

    @Id
    private String email;

    @Column
    private String fullName;

    @Column
    private String avatar;

    @Column
    private String address;

    @Column
    private String phoneNumber;

    @Column
    private long totalBuy = 0;

    @Column
    @JsonIgnore
    private Timestamp updDate;

//    @OneToMany(cascade = CascadeType.ALL,
//               mappedBy = "category", orphanRemoval = true)
//    @JsonIgnore
//    private List<Product> products = new ArrayList<>();

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }

    public void copyFieldValues(Customer customer) {
        this.email = customer.email;
        this.fullName = customer.fullName;
        this.avatar = customer.avatar;
        this.address = customer.address;
        this.phoneNumber = customer.phoneNumber;
        this.totalBuy = customer.totalBuy;
    }
}
