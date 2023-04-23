package com.szallasd.szallasok.service;

import com.szallasd.szallasok.controller.UserController;
import com.szallasd.szallasok.model.Felhasznalo;
import com.szallasd.szallasok.model.FoglalasiElozmenyek;
import com.szallasd.szallasok.model.FoglalhatoSzallasok;
import com.szallasd.szallasok.repository.FoglalasRepo;
import com.szallasd.szallasok.repository.FoglalasiElozmenyekRepo;
import com.szallasd.szallasok.repository.FoglalhatoSzallasokRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FoglalasModositas {
    private final FoglalasiElozmenyekRepo foglalasiElozmenyekRepo;
    private final FoglalhatoSzallasokRepo foglalhatoSzallasok;
    private final FoglalasRepo foglalasRepo;
    public Felhasznalo bejelentkezett;
    @Autowired
    private EmailKuldes emailKuldes;

    public FoglalasModositas(EmailKuldes emailKuldes, FoglalasiElozmenyekRepo foglalasiElozmenyekRepo, FoglalhatoSzallasokRepo foglalhatoSzallasok, FoglalasRepo foglalasRepo) {
        this.emailKuldes = emailKuldes;
        this.foglalasiElozmenyekRepo = foglalasiElozmenyekRepo;
        this.foglalhatoSzallasok = foglalhatoSzallasok;
        this.foglalasRepo = foglalasRepo;
    }

    public String modositas(FoglalasiElozmenyek foglalas, LocalDate kezdo, LocalDate vege, Integer szemelyMennyiseg) {
        FoglalhatoSzallasok szallas = foglalhatoSzallasok.findById(foglalas.getSzallasIDElozmeny()).get();
        int ujFizetendo;
        int ujNapmennyiseg;
        String uzenet = "";
        LocalDate ujKezdo = foglalas.getKezdo_idopontja();
        LocalDate ujVegzo = foglalas.getVegzo_idopont();
        int ujSzemelyMennyiseg = foglalas.getSzemelyMennyiseg();
        LocalDate maiDatum = LocalDate.now();
        if(kezdo.isEqual(vege)) {
            uzenet = "Nem adhatod meg ugyanazt a dátumot beköltözés és kiköltözés napjának, minimum 1 napra tudsz csak szállást bérelni!";
            return uzenet;
        }
        if(kezdo.isEqual(maiDatum)) {
            uzenet = "Nem adhatod meg a mai napot a beköltözés napjának, mindig csak következő naptól tudsz foglalni ha még van hely a szálláson!";
            return uzenet;
        }
        if (kezdo.isEqual(ujKezdo) && vege.isEqual(ujVegzo) && szemelyMennyiseg == ujSzemelyMennyiseg) {
            uzenet = "Nem történt módosítás!";
            return uzenet;
        }
        if (kezdo.isAfter(vege)) {
            uzenet = "Nem lehet későbbre tenni az érkezés dátumát mint a kiköltözés dátuma!";
            return uzenet;
        }
        if (kezdo.isBefore(maiDatum) || vege.isBefore(maiDatum)) {
            uzenet = "Nem lehet a mai dátumnál korábbra foglalni!";
            return uzenet;
        }
        if (!(kezdo.isEqual(ujKezdo))) {
            ujKezdo = kezdo;
        }

        if (!(vege.isEqual(ujVegzo))) {
            ujVegzo = vege;
        }
        if (szemelyMennyiseg != ujSzemelyMennyiseg) {
            ujSzemelyMennyiseg = szemelyMennyiseg;
        }
        if (ujSzemelyMennyiseg > szallas.getMaxFerohely()) {
            uzenet = "A megadott személyek száma meghaladja a szállás maxiumum " + szallas.getMaxFerohely() + " fős kapacitását!";
            return uzenet;
        }
        ujNapmennyiseg = Math.toIntExact(ChronoUnit.DAYS.between(ujKezdo, ujVegzo));
        ujFizetendo = (szallas.getArPerNapPerFo() * ujSzemelyMennyiseg) * ujNapmennyiseg;
        Integer ferohely = foglalasiElozmenyekRepo.szemelyekSzamaAzIdopontokKozott(ujKezdo, ujVegzo, szallas.getId());
        if (ferohely == null) {
            ferohely = 0;
        }
        Integer helyek = foglalasiElozmenyekRepo.modositandoSzemelyMennyisegMegadottDatumokKozott(ujKezdo, ujVegzo, foglalas.getId(), szallas.getId());
        if (helyek == null) {
            helyek = 0;
        }
        ferohely -= helyek;
        Integer marFoglalt = ferohely;
        int szemelyMaxMehet = szallas.getMaxFerohely() - ferohely;
        ferohely += ujSzemelyMennyiseg;

        if (ferohely <= szallas.getMaxFerohely()) {
            foglalasiElozmenyekRepo.frissitesFoglalasElozmeny(ujKezdo, ujVegzo, ujFizetendo, ujNapmennyiseg, ujSzemelyMennyiseg, foglalas.getId());
            foglalasRepo.frissitesFoglalas(ujKezdo, ujVegzo, ujFizetendo, ujNapmennyiseg, ujSzemelyMennyiseg, foglalas.getId());
            uzenet = "Sikeres módosítás!";
            bejelentkezett = UserController.getBejelentkezettFelhasznalo();
            emailKuldes.beregisztaltEmail(bejelentkezett.getEmail(), "A '" + foglalas.getId() + "' számú azonosítójú foglalásod az alábbiak szerint módosúlt\n\nSzállás neve: " + szallas.getNev()
                    + "\n\nBeköltözés napja: " + ujKezdo + "\n\nKiköltözés napja: " + ujVegzo + "\n\nSzemélyek száma: " + szemelyMennyiseg + "\n\nFizetendő összeg: " + ujFizetendo + " Ft\n\n\n\nA szállás " + ujKezdo + " napon 12:00-tól költözhető be!\nKérjük, hogy "
                    + ujVegzo + " napon legkésőbb délig adják át a kulcsot a kiköltözés után!", "Foglalásod módosult!");
        } else {
            boolean talaltJoDatumotElore = false;
            LocalDate ujKezdoElore = ujKezdo;
            LocalDate ujVegzoElore = ujVegzo;
            while (!talaltJoDatumotElore) {
                ujKezdoElore = ujKezdoElore.plusDays(1);
                ujVegzoElore = ujVegzoElore.plusDays(1);
                ferohely = foglalasiElozmenyekRepo.szemelyekSzamaAzIdopontokKozott(ujKezdoElore, ujVegzoElore, szallas.getId());
                if (ferohely == null) {
                    ferohely = 0;
                }
                helyek = foglalasiElozmenyekRepo.modositandoSzemelyMennyisegMegadottDatumokKozott(ujKezdoElore, ujVegzoElore, foglalas.getId(), szallas.getId());
                if (helyek == null) {
                    helyek = 0;
                }
                ferohely -= helyek;
                ferohely += ujSzemelyMennyiseg;
                if (ferohely <= szallas.getMaxFerohely()) {
                    talaltJoDatumotElore = true;
                }
            }

            boolean talaltJoDatumotHatra = false;
            boolean tudHatra = true;
            LocalDate ujKezdoHatra = ujKezdo;
            LocalDate ujVegzoHatra = ujVegzo;
            while (!talaltJoDatumotHatra) {
                ujKezdoHatra = ujKezdoHatra.minusDays(1);
                ujVegzoHatra = ujVegzoHatra.minusDays(1);
                if (ujKezdoHatra.isEqual(maiDatum)) {
                    tudHatra = false;
                    break;
                }
                ferohely = foglalasiElozmenyekRepo.szemelyekSzamaAzIdopontokKozott(ujKezdoHatra, ujVegzoHatra, szallas.getId());
                if (ferohely == null) {
                    ferohely = 0;
                }
                helyek = foglalasiElozmenyekRepo.modositandoSzemelyMennyisegMegadottDatumokKozott(ujKezdoHatra, ujVegzoHatra, foglalas.getId(), szallas.getId());
                if (helyek == null) {
                    helyek = 0;
                }
                ferohely -= helyek;
                ferohely += ujSzemelyMennyiseg;
                if (ferohely <= szallas.getMaxFerohely()) {
                    talaltJoDatumotHatra = true;
                }
            }
            if (szemelyMaxMehet <= 0) {
                if (tudHatra) {
                    uzenet = "Sajnos erre az időszakra már minden helyet lefoglaltak a szálláson! A Legközelebbi szabad időpont a kívánt időponthoz képest " + ujNapmennyiseg + " napra " + ujSzemelyMennyiseg + " főnek " + ujKezdoElore + "-tól, vagy ha előbb szeretnél akkor már " + ujKezdoHatra + "-tól lehetséges!";
                }
                if(!tudHatra) {
                    uzenet = "Sajnos erre az időszakra már minden helyet lefoglaltak a szálláson! A Legközelebbi szabad időpont a kívánt időponthoz képest " + ujNapmennyiseg + " napra " + ujSzemelyMennyiseg + " főnek " + ujKezdoElore + "-tól lehetséges, előbb már sajnos nincs lehetőség!";
                }
            } else {
                if(tudHatra) {
                    uzenet = "Sajnos erre az időszakra már csak " + szemelyMaxMehet + " személynek foglalható ez a szállás, mert a többi " + marFoglalt + " helyet már lefoglalták! A Legközelebbi szabad időpont a kívánt időponthoz képest " + ujNapmennyiseg + " napra " + ujSzemelyMennyiseg + " főnek " + ujKezdoElore + "-tól, vagy ha előbb szeretnél akkor már " + ujKezdoHatra + "-tól lehetséges!";
                }
                if(!tudHatra) {
                    uzenet = "Sajnos erre az időszakra már csak " + szemelyMaxMehet + " személynek foglalható ez a szállás, mert a többi " + marFoglalt + " helyet már lefoglalták! A Legközelebbi szabad időpont a kívánt időponthoz képest " + ujNapmennyiseg + " napra " + ujSzemelyMennyiseg + " főnek " + ujKezdoElore + "-tól lehetséges, előbb már sajnos nincs lehetőség!";
                }
                }
        }
        return uzenet;
    }
}
