package com.scttshop.api.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="UserAccount")
public class UserAccount implements Serializable {

    @Id
    private String username;

    @Column
    //@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column
    private String role;

    @Column
    private String fullName;

    @Column
    private String avatar;

    @Column
    private String email;

    @Column
    //@JsonFormat(pattern="dd-MM-yyyy")
    private Date birthDate;

    @Column
    private String address;

    @Column
    private String phoneNumber;

    @Column(nullable = true)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Timestamp lastLoginTime;

    @Column
    private int status = 1;

    @Column
    private int isVerified = 0;

    @Column
    @JsonIgnore
    private Timestamp updDate;

    public String getBirthDate(){
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return birthDate.toLocalDate().format(formatter);
        }
        catch (Exception e) {
            return "";
        }
    }

    public String getUpdDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return updDate.toLocalDateTime().format(formatter);
    }

    public String getLastLoginTime(){
        try {
            if (lastLoginTime == null || String.valueOf(lastLoginTime).isEmpty())
                return "";

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return lastLoginTime.toLocalDateTime().format(formatter);
        }
        catch (Exception e){
            return "";
        }
    }

    public void setBirthDate(String birthDate){
        try {
            this.birthDate = new java.sql.Date((new SimpleDateFormat("dd-MM-yyyy").parse(birthDate)).getTime());;
        }
        catch (ParseException e) {
            this.birthDate = null;
        }
    }

    public void copyFieldValues(UserAccount user) {
        this.password = user.password;
        this.role = user.role;
        this.fullName = user.fullName;
        this.address = user.address;
        this.phoneNumber = user.phoneNumber;
        //this.lastLoginTime = user.lastLoginTime;
        this.status = 1;
        this.isVerified = 1;
        this.birthDate = user.birthDate;
        this.avatar = user.avatar;
        this.email = user.email;
    }
}
