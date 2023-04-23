package com.szallasd.szallasok.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "felhasznalo")
public class Felhasznalo {
    @Id
    @Column(name = "email", nullable = false, length = 128)
    private String email;

    @Column(name = "felhasznalo_tipus", nullable = false, length = 128)
    private String felhasznaloTipus;

    @Column(name = "nev", nullable = false, length = 128)
    private String nev;

    @Column(name = "lakcim", nullable = false, length = 128)
    private String lakcim;

    @Column(name = "telefonszam", nullable = false, length = 128)
    private String telefonszam;

    @Column(name = "jelszo", nullable = false, length = 128)
    private String jelszo;

    public Felhasznalo() {
    }

    public Felhasznalo(String email, String felhasznaloTipus, String nev, String lakcim, String telefonszam, String jelszo) {
        this.email = email;
        this.felhasznaloTipus = felhasznaloTipus;
        this.nev = nev;
        this.lakcim = lakcim;
        this.telefonszam = telefonszam;
        this.jelszo = jelszo;
    }

    public String getJelszo() {
        return jelszo;
    }

    public void setJelszo(String jelszo) {
        this.jelszo = jelszo;
    }

    public String getTelefonszam() {
        return telefonszam;
    }

    public void setTelefonszam(String telefonszam) {
        this.telefonszam = telefonszam;
    }

    public String getLakcim() {
        return lakcim;
    }

    public void setLakcim(String lakcim) {
        this.lakcim = lakcim;
    }

    public String getNev() {
        return nev;
    }

    public void setNev(String nev) {
        this.nev = nev;
    }

    public String getFelhasznaloTipus() {
        return felhasznaloTipus;
    }

    public void setFelhasznaloTipus(String felhasznaloTipus) {
        this.felhasznaloTipus = felhasznaloTipus;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Felhasznalo{" +
                "email='" + email + '\'' +
                ", felhasznaloTipus='" + felhasznaloTipus + '\'' +
                ", nev='" + nev + '\'' +
                ", lakcim='" + lakcim + '\'' +
                ", telefonszam='" + telefonszam + '\'' +
                ", jelszo='" + jelszo + '\'' +
                '}';
    }
}
