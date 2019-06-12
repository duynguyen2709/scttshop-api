package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import static com.scttshop.api.Cache.CacheFactoryManager.CUSTOMER_CACHE;

@Data @NoArgsConstructor @AllArgsConstructor @Entity(name = "Comment")
public class Comment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int commentID;

    @Column
    private int productID;

    @Column(nullable = true)
    private String email;

    @Column
    private String customerName;

    @Column
    private String comment;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp commentTime;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "productID", insertable = false, updatable = false, nullable = false)
//    @JsonIgnore
//    private Product product;

    @Column
    @JsonIgnore
    private Timestamp updDate;

    public String getUpdDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }

    public String getCommentTime() {
        if (commentTime == null || String.valueOf(commentTime).isEmpty()) {
            return "";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return commentTime.toLocalDateTime().format(formatter);
    }

    public void copyFieldValues(Comment comment) {
        //        this.password = user.password;
        //        this.role = user.role;
        //        this.fullName = user.fullName;
        //        this.address = user.address;
        //        this.phoneNumber = user.phoneNumber;
        //        this.commentTime = user.commentTime;
    }
}
