package com.szallasd.szallasok.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ertekelesek")
public class Ertekelesek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ertekelesID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "szallasID", nullable = false)
    private FoglalhatoSzallasok szallasID;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "ertekelo_email_cime", nullable = false)
    private Felhasznalo ertekeloEmailCime;

    @Column(name = "ertekeles_idopontja", nullable = false)
    private Instant ertekelesIdopontja;

    @Column(name = "ertekeles", nullable = false)
    private Integer ertekeles;

    @Lob
    @Column(name = "ertekeles_leiras", nullable = false)
    private String ertekelesLeiras;

    public Ertekelesek() {
    }

    public Ertekelesek(FoglalhatoSzallasok szallasID, Felhasznalo ertekeloEmailCime, Instant ertekelesIdopontja, Integer ertekeles, String ertekelesLeiras) {
        this.szallasID = szallasID;
        this.ertekeloEmailCime = ertekeloEmailCime;
        this.ertekelesIdopontja = ertekelesIdopontja;
        this.ertekeles = ertekeles;
        this.ertekelesLeiras = ertekelesLeiras;
    }

    public String getErtekelesLeiras() {
        return ertekelesLeiras;
    }

    public void setErtekelesLeiras(String ertekelesLeiras) {
        this.ertekelesLeiras = ertekelesLeiras;
    }

    public Integer getErtekeles() {
        return ertekeles;
    }

    public void setErtekeles(Integer ertekeles) {
        this.ertekeles = ertekeles;
    }

    public Instant getErtekelesIdopontja() {
        return ertekelesIdopontja;
    }

    public void setErtekelesIdopontja(Instant ertekelesIdopontja) {
        this.ertekelesIdopontja = ertekelesIdopontja;
    }

    public Felhasznalo getErtekeloEmailCime() {
        return ertekeloEmailCime;
    }

    public void setErtekeloEmailCime(Felhasznalo ertekeloEmailCime) {
        this.ertekeloEmailCime = ertekeloEmailCime;
    }

    public FoglalhatoSzallasok getSzallasID() {
        return szallasID;
    }

    public void setSzallasID(FoglalhatoSzallasok szallasID) {
        this.szallasID = szallasID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Ertekelesek{" +
                "id=" + id +
                ", szallasID=" + szallasID +
                ", ertekeloEmailCime=" + ertekeloEmailCime +
                ", ertekelesIdopontja=" + ertekelesIdopontja +
                ", ertekeles=" + ertekeles +
                ", ertekelesLeiras='" + ertekelesLeiras + '\'' +
                '}';
    }
}