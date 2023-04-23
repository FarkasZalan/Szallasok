package com.szallasd.szallasok.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "foglalas")
public class Foglalas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "foglalasID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "szallasID", nullable = false)
    private FoglalhatoSzallasok szallasID;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "foglalo_email_cime", nullable = false)
    private Felhasznalo foglaloEmailCime;

    @Column(name = "foglalas_idopontja", nullable = false)
    private LocalDate foglalas_idopontja;

    @Column(name = "kezdo_idopontja", nullable = false)
    private LocalDate kezdo_idopontja;

    public LocalDate getFoglalas_idopontja() {
        return foglalas_idopontja;
    }

    public void setFoglalas_idopontja(LocalDate foglalas_idopontja) {
        this.foglalas_idopontja = foglalas_idopontja;
    }

    public LocalDate getKezdo_idopontja() {
        return kezdo_idopontja;
    }

    public void setKezdo_idopontja(LocalDate kezdo_idopontja) {
        this.kezdo_idopontja = kezdo_idopontja;
    }

    public LocalDate getVegzo_idopontja() {
        return vegzo_idopontja;
    }

    public void setVegzo_idopontja(LocalDate vegzo_idopontja) {
        this.vegzo_idopontja = vegzo_idopontja;
    }

    @Column(name = "vegzo_idopontja", nullable = false)
    private LocalDate vegzo_idopontja;

    @Column(name = "fizetendo_osszeg", nullable = false)
    private Integer fizetendoOsszeg;

    @Column(name = "szemely_mennyiseg", nullable = false)
    private Integer szemelyMennyiseg;

    @Column(name = "nap_mennyiseg", nullable = false)
    private Integer napMennyiseg;

    public Foglalas() {
    }

    public Foglalas( FoglalhatoSzallasok szallasID, Felhasznalo foglaloEmailCime, LocalDate foglalas_idopontja,  LocalDate kezdo_idopontja, LocalDate vegzo_idopontja, Integer fizetendoOsszeg, Integer szemelyMennyiseg, Integer napMennyiseg) {
        this.szallasID = szallasID;
        this.foglaloEmailCime = foglaloEmailCime;
        this.foglalas_idopontja = foglalas_idopontja;
        this.kezdo_idopontja = kezdo_idopontja;
        this.vegzo_idopontja = vegzo_idopontja;
        this.fizetendoOsszeg = fizetendoOsszeg;
        this.szemelyMennyiseg = szemelyMennyiseg;
        this.napMennyiseg = napMennyiseg;
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

    public LocalDate getFoglalas() {
        return foglalas_idopontja;
    }

    public void setFoglalas(LocalDate foglalas) {
        this.foglalas_idopontja = foglalas;
    }

    public Felhasznalo getFoglaloEmailCime() {
        return foglaloEmailCime;
    }

    public void setFoglaloEmailCime(Felhasznalo foglaloEmailCime) {
        this.foglaloEmailCime = foglaloEmailCime;
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
        return "Foglalas{" +
                "id=" + id +
                ", szallasID=" + szallasID +
                ", foglaloEmailCime=" + foglaloEmailCime +
                ", foglalas=" + foglalas_idopontja +
                ", fizetendoOsszeg=" + fizetendoOsszeg +
                ", szemelyMennyiseg=" + szemelyMennyiseg +
                ", napMennyiseg=" + napMennyiseg +
                '}';
    }
}