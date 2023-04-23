package com.szallasd.szallasok.service;

import com.szallasd.szallasok.model.Felhasznalo;
import com.szallasd.szallasok.repository.FelhasznaloRepo;
import com.szallasd.szallasok.repository.FoglalasRepo;
import com.szallasd.szallasok.repository.FoglalhatoSzallasokRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class Letrehozas {
    @Autowired
    private final FelhasznaloRepo felhasznaloRepo;
    private final FoglalasRepo foglalasRepo;
    private final FoglalhatoSzallasokRepo foglalhatoSzallasokRepo;

    public Letrehozas(FelhasznaloRepo felhasznaloRepo, FoglalasRepo foglalasRepo, FoglalhatoSzallasokRepo foglalhatoSzallasokRepo) {
        this.felhasznaloRepo = felhasznaloRepo;
        this.foglalasRepo = foglalasRepo;
        this.foglalhatoSzallasokRepo = foglalhatoSzallasokRepo;
    }

    public String felhasznaloLetrehoz(Felhasznalo felhasznalo) {
        boolean van = false;
        if (!(felhasznalo.getEmail().contains("@") && (felhasznalo.getEmail().contains(".")))) {
            return "Érvénytelen email cím, az email címben kell lennie '@'-nak és egy érvényes domainnek";
        }
        for (Felhasznalo felhasznalo1 : felhasznaloRepo.felhasznalokListaja()) {

            if (Objects.equals(felhasznalo.getEmail(), felhasznalo1.getEmail())) {
                van = true;
                break;
            }
        }
        if (!van) {
            felhasznaloRepo.save(felhasznalo);
            felhasznaloRepo.felhasznalokListaja().add(felhasznalo);
            return "Sikeres mentés!";
        }
        return "Ezzel az email címmel már regisztráltak!";
    }
}
