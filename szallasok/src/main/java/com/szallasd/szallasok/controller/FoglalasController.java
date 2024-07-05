package com.szallasd.szallasok.controller;

import com.szallasd.szallasok.model.Felhasznalo;
import com.szallasd.szallasok.model.Foglalas;
import com.szallasd.szallasok.model.FoglalasiElozmenyek;
import com.szallasd.szallasok.model.FoglalhatoSzallasok;
import com.szallasd.szallasok.repository.*;
import com.szallasd.szallasok.service.BejelentkezettBerloFoglalasai;
import com.szallasd.szallasok.service.EmailKuldes;
import com.szallasd.szallasok.service.FoglalasModositas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Controller
public class FoglalasController {
    public static List<FoglalasiElozmenyek> elozmenyek = new ArrayList<>();
    public boolean adminVanBent = false;
    public boolean kiadoVanBent = false;
    public boolean berloVanBent = false;
    public boolean senkiNincsBejelentkezve = true;
    ErtekelesekRepo ertekelesekRepo;
    FelhasznaloRepo felhasznaloRepo;
    FoglalasiElozmenyekRepo foglalasiElozmenyekRepo;
    FoglalasRepo foglalasRepo;
    FoglalhatoSzallasokRepo foglalhatoSzallasokRepo;
    Felhasznalo bejelentkezett;
    FoglalasiElozmenyek modositando;
    FoglalhatoSzallasok kapcsolodoSzallas;
    LocalDate kezdo_idopont_modositashoz;
    @Autowired
    private final EmailKuldes emailKuldes;

    public FoglalasController(ErtekelesekRepo ertekelesekRepo, FelhasznaloRepo felhasznaloRepo, FoglalasiElozmenyekRepo foglalasiElozmenyekRepo, FoglalasRepo foglalasRepo, FoglalhatoSzallasokRepo foglalhatoSzallasokRepo, EmailKuldes emailKuldes) {
        this.ertekelesekRepo = ertekelesekRepo;
        this.felhasznaloRepo = felhasznaloRepo;
        this.foglalasiElozmenyekRepo = foglalasiElozmenyekRepo;
        this.foglalasRepo = foglalasRepo;
        this.foglalhatoSzallasokRepo = foglalhatoSzallasokRepo;
        this.emailKuldes = emailKuldes;
    }

    public static List<FoglalasiElozmenyek> getElozmenyek() {
        return elozmenyek;
    }

    @GetMapping(value = "/foglalasaim")
    public String foglalasokHandler(Model model, RedirectAttributes redirectAttributes) {
        //Menühüz kell
        senkiNincsBejelentkezve = UserController.getSenkiNincsBent();
        adminVanBent = UserController.getAdminVanBent();
        kiadoVanBent = UserController.getKiadoVanBent();
        berloVanBent = UserController.getBerloVanBent();
        if (senkiNincsBejelentkezve) {
            return "redirect:/log_reg";
        }
        model.addAttribute("berlo", berloVanBent);
        model.addAttribute("ures", senkiNincsBejelentkezve);
        model.addAttribute("kiado", kiadoVanBent);
        model.addAttribute("admin", adminVanBent);
        //idáig

            BejelentkezettBerloFoglalasai foglalasok = new BejelentkezettBerloFoglalasai(foglalasiElozmenyekRepo);
            bejelentkezett = UserController.getBejelentkezettFelhasznalo();
            if (bejelentkezett == null) {
                return "/log_reg";
            }
        if(bejelentkezett.getFelhasznaloTipus().equals("Bérlő")) {
            elozmenyek = foglalasok.litazas(elozmenyek, bejelentkezett);
            if (elozmenyek.size() == 0) {
                redirectAttributes.addFlashAttribute("Uzenet", "Még nincs egy lefoglalt szállásod sem!");
                return "redirect:/";
            }
            model.addAttribute("elozmenyek", elozmenyek);
            return "foglalasaim";
        } else {
            return "redirect:/szallasok";
        }
    }

    @GetMapping(value = "foglalas_modositas")
    public String foglalas_modositas(Model model, @RequestParam(value = "foglalasAzonosito", required = false) Integer modositandoFoglalas, @RequestParam(value = "idopont", required = false) String kezdo, RedirectAttributes redirectAttributes) {
        if (bejelentkezett == null) {
            return "/log_reg";
        }
        if(bejelentkezett.getFelhasznaloTipus().equals("Bérlő")) {
            if (kezdo != null) {
                kezdo_idopont_modositashoz = LocalDate.parse(kezdo);
            }
            LocalDate datum = LocalDate.now();
            if (datum.isAfter(kezdo_idopont_modositashoz) || datum.isEqual(kezdo_idopont_modositashoz)) {
                redirectAttributes.addFlashAttribute("Uzenet", "Csak a beköltözés előtti napig módosíthatod vagy törölheted foglalásodat!");
                return "redirect:/foglalasaim";
            }
            if (modositandoFoglalas != null) {
                modositando = foglalasiElozmenyekRepo.findById(modositandoFoglalas).get();
                boolean toroltekMarASzallast = true;
                for (Foglalas foglalas : foglalasRepo.foglalasokListaja()) {
                    if (Objects.equals(foglalas.getId(), modositando.getId())) {
                        toroltekMarASzallast = false;
                        break;
                    }
                }
                if (toroltekMarASzallast) {
                    redirectAttributes.addFlashAttribute("Uzenet", "Ez a foglalás sajnos már törlésre került a kiadó által szóval sem módosítani, sem törölni nem tudod már!");
                    return "redirect:/foglalasaim";
                }
                kapcsolodoSzallas = foglalhatoSzallasokRepo.findById(modositando.getSzallasIDElozmeny()).get();
            }

            Integer ferohely = foglalasiElozmenyekRepo.szemelyekSzamaAzIdopontokKozott(modositando.getKezdo_idopontja(), modositando.getVegzo_idopont(), modositando.getSzallasIDElozmeny());
            if (ferohely == null) {
                ferohely = 0;
            }
            ferohely = kapcsolodoSzallas.getMaxFerohely() - ferohely;
            model.addAttribute("szallas", modositando);
            model.addAttribute("szallasKapacitas", ferohely);
            return "foglalas_modositas";
        } else {
            return "redirect:/szallasok";
        }
    }

    @GetMapping(value = "foglalas_modosit")
    public String foglalas_modosit(RedirectAttributes redirectAttributes,
                                   @RequestParam(value = "date_start", required = false) String kezdo,
                                   @RequestParam(value = "date_end", required = false) String vegzo,
                                   @RequestParam(value = "dbszam", required = false) Integer mennyiseg) {
        if (bejelentkezett == null) {
            return "/log_reg";
        }
        if(bejelentkezett.getFelhasznaloTipus().equals("Bérlő")) {
            boolean toroltekMarASzallast = true;
            for (Foglalas foglalas : foglalasRepo.foglalasokListaja()) {
                if (Objects.equals(foglalas.getId(), modositando.getId())) {
                    toroltekMarASzallast = false;
                    break;
                }
            }
            if (toroltekMarASzallast) {
                redirectAttributes.addFlashAttribute("Uzenet", "Ez a foglalás sajnos már törlésre került a kiadó által szóval sem módosítani, sem törölni nem tudod már!");
                return "redirect:/foglalasaim";
            }
            if (Objects.equals(kezdo, "") || Objects.equals(vegzo, "")) {
                redirectAttributes.addFlashAttribute("Uzenet", "Érvénytelen dátumot adtál meg!");
                return "redirect:/foglalas_modositas";
            }
            FoglalasModositas modositas = new FoglalasModositas(emailKuldes, foglalasiElozmenyekRepo, foglalhatoSzallasokRepo, foglalasRepo);
            BejelentkezettBerloFoglalasai foglalasok = new BejelentkezettBerloFoglalasai(foglalasiElozmenyekRepo);
            LocalDate kezdo_idopont = LocalDate.parse(kezdo);
            LocalDate vegzo_idopont = LocalDate.parse(vegzo);
            if (mennyiseg == null) {
                mennyiseg = 1;
            }
            modositando.setBejelentkezettFelhasznalo(bejelentkezett);

            String uzenet = modositas.modositas(modositando, kezdo_idopont, vegzo_idopont, mennyiseg);
            modositando = foglalasiElozmenyekRepo.findById(modositando.getId()).get();
            elozmenyek.clear();
            elozmenyek = foglalasok.litazas(elozmenyek, bejelentkezett);
            redirectAttributes.addFlashAttribute("Uzenet", uzenet);
            return "redirect:/foglalas_modositas";
        } else {
            return "redirect:/szallasok";
        }
    }

    @GetMapping(value = "foglalas_torlese")
    public String foglalas_torlese(@RequestParam("foglalasAzonosito") Integer torlendoFoglalas, @RequestParam("idopont") String kezdo, RedirectAttributes redirectAttributes) {
        boolean toroltekMarASzallast = true;
        for (Foglalas foglalas : foglalasRepo.foglalasokListaja()) {
            if (Objects.equals(foglalas.getId(), torlendoFoglalas)) {
                toroltekMarASzallast = false;
                break;
            }
        }
        if (toroltekMarASzallast) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ez a foglalás sajnos már törlésre került a kiadó által szóval sem módosítani, sem törölni nem tudod már!");
            return "redirect:/foglalasaim";
        }
        LocalDate kezdo_idopont = LocalDate.parse(kezdo);
        LocalDate datum = LocalDate.now();
        if (datum.isAfter(kezdo_idopont) || datum.isEqual(kezdo_idopont)) {
            redirectAttributes.addFlashAttribute("Uzenet", "Csak a beköltözés előtti napig módosíthatod vagy törölheted foglalásodat!");
            return "redirect:/foglalasaim";
        }
        FoglalasiElozmenyek torlendo = foglalasiElozmenyekRepo.findById(torlendoFoglalas).get();
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        String pontosDatum = dateFormat.format(date);
        emailKuldes.beregisztaltEmail(bejelentkezett.getEmail(), "A '" + torlendo.getId() + "' számú azonosítójú foglalásod " + pontosDatum + "-kor lemondásra került!\n\n\n\nSzállás neve: " + torlendo.getSzallasNeve() + "\n\nLemondott szállás időintervalluma:" + torlendo.getKezdo_idopontja() + "-tól " + torlendo.getVegzo_idopont() + "-ig\n\nSajnáljuk, hogy nem jött össze a mostani foglalásod, de reméljük, hogy hamarosan újra nálunk foglalsz.\n\nÜdvüzlettel: A Szállás Neked csapata", "Foglalás lemondása");
        foglalasiElozmenyekRepo.deleteById(torlendoFoglalas);

        foglalasRepo.deleteById(torlendoFoglalas);
        elozmenyek.clear();
        emailKuldes.beregisztaltEmail(torlendo.getKiadoEmail(), "Sajnálattal értesítjük, hogy a " + torlendo.getSzallasNeve() + "hoz tartozó " + torlendo.getKezdo_idopontja() + " - " + torlendo.getVegzo_idopont() + " közötti foglalást lemondták!\n\nFoglaló email címe: "+ bejelentkezett.getEmail() + "\nSzemélyek száma: " + torlendo.getSzemelyMennyiseg() + "\n\nÜdvüzlettel: A Szállás Neked csapata", "Foglalás lemondása");
        redirectAttributes.addFlashAttribute("Uzenet", "Sikeresen lemondtad a foglalásod!");
        return "redirect:/foglalasaim";
    }

    @GetMapping(value = "/foglalas")
    public String foglalas(Model model, RedirectAttributes redirectAttributes) {
        //Menühüz kell
        senkiNincsBejelentkezve = UserController.getSenkiNincsBent();
        adminVanBent = UserController.getAdminVanBent();
        kiadoVanBent = UserController.getKiadoVanBent();
        berloVanBent = UserController.getBerloVanBent();
        model.addAttribute("berlo", berloVanBent);
        model.addAttribute("ures", senkiNincsBejelentkezve);
        model.addAttribute("kiado", kiadoVanBent);
        model.addAttribute("admin", adminVanBent);
        //idáig

        return "foglalas";
    }
}
