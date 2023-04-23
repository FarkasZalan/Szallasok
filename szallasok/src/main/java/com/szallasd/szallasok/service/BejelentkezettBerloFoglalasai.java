package com.szallasd.szallasok.service;


import com.szallasd.szallasok.model.Felhasznalo;
import com.szallasd.szallasok.model.FoglalasiElozmenyek;
import com.szallasd.szallasok.repository.FelhasznaloRepo;
import com.szallasd.szallasok.repository.FoglalasiElozmenyekRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BejelentkezettBerloFoglalasai {
    @Autowired
    private final FoglalasiElozmenyekRepo foglalasiElozmenyekRepo;

    public BejelentkezettBerloFoglalasai(FoglalasiElozmenyekRepo foglalasiElozmenyekRepo) {
        this.foglalasiElozmenyekRepo = foglalasiElozmenyekRepo;
    }

    public List<FoglalasiElozmenyek> litazas( List<FoglalasiElozmenyek> lista, Felhasznalo bejelentkezett) {
        boolean marBenneVan = false;
        for(FoglalasiElozmenyek elozmenyek : foglalasiElozmenyekRepo.foglalasiElozmenyekListaja()) {
            if(elozmenyek.getBejelentkezettFelhasznalo().getEmail().equals(bejelentkezett.getEmail())) {
                for(FoglalasiElozmenyek benneLevo : lista) {
                    if(Objects.equals(benneLevo.getId(), elozmenyek.getId())) {
                        marBenneVan = true;
                        break;
                    }
                    marBenneVan = false;
                }
                if(!marBenneVan) {
                    lista.add(elozmenyek);
                }
            }
        }
        return lista;
    }
}
