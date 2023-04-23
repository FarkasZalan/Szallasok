package com.szallasd.szallasok.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "foglalasi_elozmenyek")
public class FoglalasiElozmenyek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "foglalasiElozmenyID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "felhasznaloEmailCime", nullable = false)
    private Felhasznalo bejelentkezett;

    @Column(name = "foglalasIdopontja", nullable = false)
    private LocalDate foglalasIdopontja;

    @Column(name = "vegzo_idopont", nullable = false)
    private LocalDate vegzo_idopont;

    public LocalDate getVegzo_idopont() {
        return vegzo_idopont;
    }

    public void setVegzo_idopont(LocalDate vegzo_idopont) {
        this.vegzo_idopont = vegzo_idopont;
    }

    public LocalDate getKezdo_idopontja() {
        return kezdo_idopontja;
    }

    public void setKezdo_idopontja(LocalDate kezdo_idopontja) {
        this.kezdo_idopontja = kezdo_idopontja;
    }

    @Column(name = "kezdo_idopontja", nullable = false)
    private LocalDate kezdo_idopontja;

    @Column(name = "fizetendoOsszeg", nullable = false)
    private Integer fizetendoOsszeg;

    @Column(name = "szemelyMennyiseg", nullable = false)
    private Integer szemelyMennyiseg;

    @Column(name = "napMennyiseg", nullable = false)
    private Integer napMennyiseg;

    public String getSzallasNeve() {
        return szallasNeve;
    }

    public void setSzallasNeve(String szallasNeve) {
        this.szallasNeve = szallasNeve;
    }

    @Column(name = "szallasNeve", nullable = false)
    private String szallasNeve;

    public String getSzallasKepe() {
        return szallasKepe;
    }

    public void setSzallasKepe(String szallasKepe) {
        this.szallasKepe = szallasKepe;
    }

    @Lob
    @Column(name = "szallasKepe", nullable = false)
    private String szallasKepe;

    public String getKiadoEmail() {
        return kiadoEmail;
    }

    public void setKiadoEmail(String kiadoEmail) {
        this.kiadoEmail = kiadoEmail;
    }

    @Column(name = "kiadoEmail", nullable = false)
    private String kiadoEmail;

    @Column(name = "szallasId", nullable = false)
    private Integer szallasId;

    public FoglalasiElozmenyek() {
    }

    public FoglalasiElozmenyek(Felhasznalo bejelentkezett, LocalDate foglalasIdopontja,LocalDate kezdo_idopontja,LocalDate vegzo_idopont, Integer fizetendoOsszeg, Integer szemelyMennyiseg, Integer napMennyiseg, String szallasNeve, Integer szallasId, String szallasKepe, String kiadoEmail) {
        this.bejelentkezett = bejelentkezett;
        this.foglalasIdopontja = foglalasIdopontja;
        this.kezdo_idopontja = kezdo_idopontja;
        this.vegzo_idopont = vegzo_idopont;
        this.fizetendoOsszeg = fizetendoOsszeg;
        this.szemelyMennyiseg = szemelyMennyiseg;
        this.napMennyiseg = napMennyiseg;
        this.szallasNeve = szallasNeve;
        this.szallasId = szallasId;
        this.szallasKepe = szallasKepe;
        this.kiadoEmail = kiadoEmail;
    }

    public Integer getNapMennyiseg() {
        return napMennyiseg;
    }

    public void setNapMennyiseg(Integer napMennyiseg) {
        this.napMennyiseg = napMennyiseg;
    }

    public Integer getSzemelyMennyiseg() {
        return szemelyMennyiseg;
    }

    public void setSzemelyMennyiseg(Integer szemelyMennyiseg) {
        this.szemelyMennyiseg = szemelyMennyiseg;
    }

    public Integer getFizetendoOsszeg() {
        return fizetendoOsszeg;
    }

    public void setFizetendoOsszeg(Integer fizetendoOsszeg) {
        this.fizetendoOsszeg = fizetendoOsszeg;
    }

    public LocalDate getFoglalasIdopontja() {
        return foglalasIdopontja;
    }

    public Felhasznalo getBejelentkezett() {
        return bejelentkezett;
    }

    public void setBejelentkezett(Felhasznalo bejelentkezett) {
        this.bejelentkezett = bejelentkezett;
    }

    public Integer getSzallasIDElozmeny() {
        return szallasId;
    }

    public void setSzallasIDElozmeny(Integer szallasID) {
        this.szallasId = szallasID;
    }

    public void setFoglalasIdopontja(LocalDate foglalasIdopontja) {
        this.foglalasIdopontja = foglalasIdopontja;
    }

    public Felhasznalo getBejelentkezettFelhasznalo() {
        return bejelentkezett;
    }

    public void setBejelentkezettFelhasznalo(Felhasznalo felhasznaloEmailCime) {
        this.bejelentkezett = felhasznaloEmailCime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "FoglalasiElozmenyek{" +
                "id=" + id +
                ", felhasznaloEmailCime=" + bejelentkezett +
                ", foglalasIdopontja=" + foglalasIdopontja +
                ", fizetendoOsszeg=" + fizetendoOsszeg +
                ", szemelyMennyiseg=" + szemelyMennyiseg +
                ", napMennyiseg=" + napMennyiseg +
                '}';
    }
}