package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="UserAccount")
public class UserAccount implements Serializable {

    @Id
    private String username;

    @Column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column
    private String role;

    @Column
    private String fullName;

    @Column
    private String address;

    @Column
    private String phoneNumber;

    @Column(nullable = true)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Timestamp lastLoginTime;

    @Column
    @JsonIgnore
    private Timestamp updDate;

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }

    public String getLastLoginTime(){
        if (lastLoginTime == null || String.valueOf(lastLoginTime).isEmpty())
            return "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return lastLoginTime.toLocalDateTime().format(formatter);
    }

    public void copyFieldValues(UserAccount user) {
        this.password = user.password;
        this.role = user.role;
        this.fullName = user.fullName;
        this.address = user.address;
        this.phoneNumber = user.phoneNumber;
        this.lastLoginTime = user.lastLoginTime;
    }
}