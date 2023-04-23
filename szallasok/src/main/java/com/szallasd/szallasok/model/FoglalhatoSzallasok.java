package com.szallasd.szallasok.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Table(name = "foglalhato_szallasok")
public class FoglalhatoSzallasok {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "szallasID", nullable = false)
    private Integer id;

    @Column(name = "nev", nullable = false, length = 200)
    private String nev;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "kiado_email", nullable = false)
    private Felhasznalo kiado;

    @Column(name = "helyszin", nullable = false, length = 200)
    private String helyszin;

    @Lob
    @Column(name = "leiras", nullable = false)
    private String leiras;

    public String getKep() {
        return kep;
    }

    public void setKep(String kep) {
        this.kep = kep;
    }

    @Lob
    @Column(name = "kep", nullable = false, columnDefinition = "MEDIUMBLOB")
    private String kep;

    @Column(name = "ar_per_nap_per_fo", nullable = false)
    private Integer arPerNapPerFo;

    public Integer getMaxFerohely() {
        return maxFerohely;
    }

    public void setMaxFerohely(Integer maxFerohely) {
        this.maxFerohely = maxFerohely;
    }

    @Column(name = "max_ferohely", nullable = false)
    private Integer maxFerohely;

    public FoglalhatoSzallasok() {
    }

    public FoglalhatoSzallasok(String nev, Felhasznalo kiadoEmail, String helyszin, String leiras, Integer arPerNapPerFo, Integer maxFerohely, String kep) {
        this.nev = nev;
        this.kiado = kiadoEmail;
        this.helyszin = helyszin;
        this.leiras = leiras;
        this.arPerNapPerFo = arPerNapPerFo;
        this.maxFerohely = maxFerohely;
        this.kep = kep;
    }

    public Integer getArPerNapPerFo() {
        return arPerNapPerFo;
    }

    public void setArPerNapPerFo(Integer arPerNapPerFo) {
        this.arPerNapPerFo = arPerNapPerFo;
    }

    public String getLeiras() {
        return leiras;
    }

    public void setLeiras(String leiras) {
        this.leiras = leiras;
    }

    public String getHelyszin() {
        return helyszin;
    }

    public void setHelyszin(String helyszin) {
        this.helyszin = helyszin;
    }

    public Felhasznalo getKiado() {
        return kiado;
    }

    public void setKiado(Felhasznalo kiadoEmail) {
        this.kiado = kiadoEmail;
    }

    public String getNev() {
        return nev;
    }

    public void setNev(String nev) {
        this.nev = nev;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "FoglalhatoSzallasok{" +
                "id=" + id +
                ", nev='" + nev + '\'' +
                ", kiadoEmail=" + kiado +
                ", helyszin='" + helyszin + '\'' +
                ", leiras='" + leiras + '\'' +
                ", arPerNapPerFo=" + arPerNapPerFo +
                '}';
    }
}