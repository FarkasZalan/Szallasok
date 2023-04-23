package com.szallasd.szallasok.service;

import com.szallasd.szallasok.model.Felhasznalo;
import com.szallasd.szallasok.repository.FelhasznaloRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public class Beleptet {
    @Autowired
    private final FelhasznaloRepo felhasznaloRepo;

    public Beleptet(FelhasznaloRepo felhasznaloRepo) {
        this.felhasznaloRepo = felhasznaloRepo;
    }

    public String belepes(String email, String jelszo) {
        String uzenet = "";
        for (Felhasznalo felhasznalo : felhasznaloRepo.felhasznalokListaja()) {
            if (Objects.equals(felhasznalo.getEmail(), email)) {
                uzenet = "Van email";
            }
            if (Objects.equals(felhasznalo.getJelszo(), jelszo) && Objects.equals(felhasznalo.getEmail(), email)) {
                uzenet = "Sikeres belépés";
                break;
            }
        }
        return uzenet;
    }

    public Felhasznalo felhasznaloMegtalal(String email) {
        Felhasznalo felhasznalo = new Felhasznalo("", "", "", "", "", "");
        for (Felhasznalo felhasznalo1 : felhasznaloRepo.felhasznalokListaja()) {
            if (felhasznalo1.getEmail().equals(email)) {
                felhasznalo.setEmail(felhasznalo1.getEmail());
                felhasznalo.setFelhasznaloTipus(felhasznalo1.getFelhasznaloTipus());
                felhasznalo.setNev(felhasznalo1.getNev());
                felhasznalo.setLakcim(felhasznalo1.getLakcim());
                felhasznalo.setTelefonszam(felhasznalo1.getTelefonszam());
                felhasznalo.setJelszo(felhasznalo1.getJelszo());
            }
        }
        return felhasznalo;
    }
}
