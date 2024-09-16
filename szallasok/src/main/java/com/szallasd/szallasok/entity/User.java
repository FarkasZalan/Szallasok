package com.szallasd.szallasok.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
// User table is 'user'
@Table(name = "user")
public class User {
    // I used the users ID as the user email
    // (need to make a better solution eg. generated number as id)
    @Id
    @Column(name = "email", nullable = false, length = 128)
    private String email;

    // this column will store the user's type (renter, admin, host)
    @Column(name = "user_type", nullable = false, length = 128)
    private String userType;

    // this column will store the user's name
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    // this column will store the user's address
    @Column(name = "address", nullable = false, length = 128)
    private String address;

    // this column will store the user's phone
    @Column(name = "phone", nullable = false, length = 128)
    private String phone;

    // this column will store the user's password
    @Column(name = "password", nullable = false, length = 128)
    private String password;

    public User() {
    }

    public User(String email, String userType, String name, String address, String phone, String password) {
        this.email = email;
        this.userType = userType;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String jelszo) {
        this.password = jelszo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String telefonszam) {
        this.phone = telefonszam;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String lakcim) {
        this.address = lakcim;
    }

    public String getName() {
        return name;
    }

    public void setName(String nev) {
        this.name = nev;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String felhasznaloTipus) {
        this.userType = felhasznaloTipus;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
