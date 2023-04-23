package com.szallasd.szallasok.controller;

import com.szallasd.szallasok.model.Ertekelesek;
import com.szallasd.szallasok.model.Felhasznalo;
import com.szallasd.szallasok.model.FoglalhatoSzallasok;
import com.szallasd.szallasok.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class ErtekelesController {

    ErtekelesekRepo ertekelesekRepo;
    FelhasznaloRepo felhasznaloRepo;
    FoglalasiElozmenyekRepo foglalasiElozmenyekRepo;
    FoglalasRepo foglalasRepo;
    FoglalhatoSzallasokRepo foglalhatoSzallasokRepo;
    public boolean adminVanBent = false;
    public boolean kiadoVanBent = false;
    public boolean berloVanBent = false;
    public boolean senkiNincsBejelentkezve = true;
    Felhasznalo bejelentkezett;

    public ErtekelesController(ErtekelesekRepo ertekelesekRepo, FelhasznaloRepo felhasznaloRepo, FoglalasiElozmenyekRepo foglalasiElozmenyekRepo, FoglalasRepo foglalasRepo, FoglalhatoSzallasokRepo foglalhatoSzallasokRepo) {
        this.ertekelesekRepo = ertekelesekRepo;
        this.felhasznaloRepo = felhasznaloRepo;
        this.foglalasiElozmenyekRepo = foglalasiElozmenyekRepo;
        this.foglalasRepo = foglalasRepo;
        this.foglalhatoSzallasokRepo = foglalhatoSzallasokRepo;
    }


    @GetMapping(value = "/ertekelesek_admin")
    public String ertekelesAdminHandler(Model model){

        if (UserController.getSenkiNincsBent()) {
            return "/log_reg";
        }
        model.addAttribute("ertekelesek", ertekelesekRepo.ertekelesekListaja() );
        return "ertekelesek_admin";
    }
    @PostMapping(value = "/ertekelesek_admin/{id}")
    public String ertkelesDelete(
            @PathVariable("id") Integer id
    ){
        //System.out.println(email +"\n");
        for (Ertekelesek ertekeles : ertekelesekRepo.ertekelesekListaja()){
            if (Objects.equals(ertekeles.getId(), id)){
                ertekelesekRepo.delete(ertekeles);
                break;
            }
        }
        return "redirect:/ertekelesek_admin";
    }

    @GetMapping(value = "/szallas/{id}/ertekel")
    public String ertekel(
            @PathVariable("id") int id,
            Model model,RedirectAttributes redirectAttributes
    ) {
        //Menühüz kell
        senkiNincsBejelentkezve = UserController.getSenkiNincsBent();
        adminVanBent = UserController.getAdminVanBent();
        kiadoVanBent = UserController.getKiadoVanBent();
        berloVanBent = UserController.getBerloVanBent();
        if(senkiNincsBejelentkezve) {
            redirectAttributes.addFlashAttribute("Uzenet", "Előbb kell jelentkezned, hogy értékelhess szállásokat!");
            return "redirect:/log_reg";
        }
        model.addAttribute("berlo", berloVanBent);
        model.addAttribute("ures", senkiNincsBejelentkezve);
        model.addAttribute("kiado", kiadoVanBent);
        model.addAttribute("admin", adminVanBent);
        //idáig

        for (FoglalhatoSzallasok sz : foglalhatoSzallasokRepo.foglalhatoSzallasokListaja())
            if (sz.getId() == id)
                model.addAttribute("szallas", sz);

        return "ertekel";
    }

    @PostMapping(value = "/szallas/{id}/ertekel")
    public String ertekelesSend(
            @PathVariable("id") int id,
            @RequestParam("text") String text,
            @RequestParam("ertekeles") int ertekeles
    ) {

        // Bejelentkezett felhasználo lekérése
        bejelentkezett = UserController.getBejelentkezettFelhasznalo();

        // Idő lekérdezése
        Instant ido = Instant.now();

        Ertekelesek uj_ertekeles = new Ertekelesek(foglalhatoSzallasokRepo.findById(id).get(), bejelentkezett, ido, ertekeles, text);
        ertekelesekRepo.save(uj_ertekeles);
        ertekelesekRepo.ertekelesekListaja().add(uj_ertekeles);

        return "redirect:/szallas/{id}";
    }

    @GetMapping(value = "/szallas/{id}/ertekelesek")
    public String ertekelesek(
            @PathVariable("id") int id,
            Model model, RedirectAttributes redirectAttributes
    ) {
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

        // Szállás lekérése
        for (FoglalhatoSzallasok sz : foglalhatoSzallasokRepo.foglalhatoSzallasokListaja())
            if (sz.getId() == id)
                model.addAttribute("szallas", sz);

        // Hozzá az értékelések

        List<Ertekelesek> ertekelesek = new ArrayList<Ertekelesek>();

        for (Ertekelesek e : ertekelesekRepo.ertekelesekListaja())
            if (e.getSzallasID().getId() == id)
                ertekelesek.add(e);

        model.addAttribute("ertekelesek", ertekelesek);
        return "ertekelesek";
    }

}
