package com.szallasd.szallasok.controller;

import com.szallasd.szallasok.model.Felhasznalo;
import com.szallasd.szallasok.model.Foglalas;
import com.szallasd.szallasok.model.FoglalasiElozmenyek;
import com.szallasd.szallasok.model.FoglalhatoSzallasok;
import com.szallasd.szallasok.repository.*;
import com.szallasd.szallasok.service.Beleptet;
import com.szallasd.szallasok.service.EmailKuldes;
import com.szallasd.szallasok.service.GoogleMapService;
import com.szallasd.szallasok.service.Letrehozas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
public class UserController {
    public static boolean adminVanBent = false;
    public static boolean kiadoVanBent = false;
    public static boolean berloVanBent = false;
    public static boolean senkiNincsBejelentkezve = true;
    static Felhasznalo bejelentkezett;
    ErtekelesekRepo ertekelesekRepo;
    FelhasznaloRepo felhasznaloRepo;
    FoglalasiElozmenyekRepo foglalasiElozmenyekRepo;
    FoglalasRepo foglalasRepo;
    FoglalhatoSzallasokRepo foglalhatoSzallasokRepo;
    boolean vanKep = true;
    String regiKep = "";
    @Autowired
    private GoogleMapService googleMapService;
    @Autowired
    private EmailKuldes emailKuldes;
    private String telepulesSzures = "";
    private String rendezesSzures = "";  // 2 értéke lehet, novekvo/csokkeno

    public UserController(ErtekelesekRepo ertekelesekRepo, FelhasznaloRepo felhasznaloRepo, FoglalasiElozmenyekRepo foglalasiElozmenyekRepo, FoglalasRepo foglalasRepo, FoglalhatoSzallasokRepo foglalhatoSzallasokRepo) {
        this.ertekelesekRepo = ertekelesekRepo;
        this.felhasznaloRepo = felhasznaloRepo;
        this.foglalasiElozmenyekRepo = foglalasiElozmenyekRepo;
        this.foglalasRepo = foglalasRepo;
        this.foglalhatoSzallasokRepo = foglalhatoSzallasokRepo;
    }

    public static boolean getSenkiNincsBent() {
        return senkiNincsBejelentkezve;
    }

    public static boolean getAdminVanBent() {
        return adminVanBent;
    }

    public static boolean getBerloVanBent() {
        return berloVanBent;
    }

    public static boolean getKiadoVanBent() {
        return kiadoVanBent;
    }

    public static Felhasznalo getBejelentkezettFelhasznalo() {
        return bejelentkezett;
    }


    @RequestMapping(value = {"/log_reg", "/login", "/reg", "/registration"})
    public String logRegHandler() {
        return "log_reg";
    }

    @RequestMapping(value = "/regisztracio_process")
    public String regisztracio_process(@RequestParam("email") String email, @RequestParam("nev") String nev, @RequestParam("lakcim") String lakcim, @RequestParam("telefonszam") String telefonszam, @RequestParam("jelszo") String jelszo, @RequestParam("jelszo2") String jelszo2, @RequestParam(name = "jogosultsag", required = false, defaultValue = "") String jogosultsag, RedirectAttributes redirectAttributes) {
        if (jogosultsag.equals("")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Jelöld be, hogy bérlő vagy kiadó szeretnél lenni!");
            return "redirect:/log_reg";
        }
        String mindenJo = "";
        boolean jelszokEgyeznek = false;
        Letrehozas letrehozas = new Letrehozas(felhasznaloRepo, foglalasRepo, foglalhatoSzallasokRepo);
        if (!jelszo.equals(jelszo2)) {
            redirectAttributes.addFlashAttribute("Uzenet", "Nem egyeznek a megadott jelszavak!");
            return "redirect:/log_reg";
        }
        if (jogosultsag.equals("Bérlő")) {
            Felhasznalo felhasznalo = new Felhasznalo(email, jogosultsag, nev, lakcim, telefonszam, jelszo);
            mindenJo = letrehozas.felhasznaloLetrehoz(felhasznalo);
        }
        if (jogosultsag.equals("Kiadó")) {
            Felhasznalo felhasznalo = new Felhasznalo(email, jogosultsag, nev, lakcim, telefonszam, jelszo);
            mindenJo = letrehozas.felhasznaloLetrehoz(felhasznalo);
        }
        if (mindenJo.equals("Sikeres mentés!")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Sikeres létrehozás! Most jelentkezz be " + nev + "!");
            emailKuldes.beregisztaltEmail(email, "Köszönjük, hogy beregisztráltál hozzánk!\n\nÜdvözlettel: A Szállás Neked csapata", "Köszöntünk az oldalunkon " + nev + "!");
        } else {
            redirectAttributes.addFlashAttribute("Uzenet", mindenJo);
        }
        return "redirect:/log_reg";
    }

    @RequestMapping(value = "/bejelentkezes")
    public String loginUser(@RequestParam("email") String email,
                            @RequestParam("psw") String jelszo,
                            RedirectAttributes redirectAttributes
    ) {
        Beleptet belepes = new Beleptet(felhasznaloRepo);
        String uzenet = belepes.belepes(email, jelszo);

        if (uzenet.isEmpty()) {
            redirectAttributes.addFlashAttribute("Uzenet", "Nincs ilyen emaillel regisztált felhasználó!");
            return "redirect:/log_reg";
        }
        if (uzenet.equals("Van email")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Rossz jelszó!");
            return "redirect:/log_reg";
        }
        if (uzenet.equals("Sikeres belépés")) {
            bejelentkezett = belepes.felhasznaloMegtalal(email);
            redirectAttributes.addFlashAttribute("Uzenet", "Sikeres belépés! Üdv újra itt, " + bejelentkezett.getNev());
            if (bejelentkezett.getFelhasznaloTipus().equals("Admin")) {
                adminVanBent = true;
            }
            if (bejelentkezett.getFelhasznaloTipus().equals("Kiadó")) {
                kiadoVanBent = true;
            }
            if (bejelentkezett.getFelhasznaloTipus().equals("Bérlő")) {
                berloVanBent = true;
            }
            senkiNincsBejelentkezve = false;
            return "redirect:/";
        }
        return "log_reg";
    }

    @RequestMapping(value = "/jelszo_emlekezteto")
    public String jelszo_emlekezteto(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes
    ) {
        Beleptet belepes = new Beleptet(felhasznaloRepo);
        Felhasznalo megtalalando = belepes.felhasznaloMegtalal(email);
        if (megtalalando.getNev().equals("")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Nincs regisztráció ezzel az email címmel: " + email + "!");
            return "redirect:/log_reg";
        } else {
            redirectAttributes.addFlashAttribute("Uzenet", "A jelszó emlékeztetőt elküldtük a megadott email címedre, " + megtalalando.getNev() + "!");
            emailKuldes.beregisztaltEmail(email, megtalalando.getNev() + ", a jelszavad a következő: " + megtalalando.getJelszo() + "\n\nÜdvözlettel: A Szállás Neked csapata", "Jelszó emlékeztető");
        }
        return "redirect:/log_reg";
    }

    @RequestMapping(value = "/users")
    public String usersHandler(Model model) {
        if (senkiNincsBejelentkezve) {
            return "log_reg";
        }
        List<Felhasznalo> felhasznalok = new ArrayList<>();
        for (Felhasznalo felhasznalo : felhasznaloRepo.felhasznalokListaja()) {
            if (!felhasznalo.getEmail().equals("admin")) {
                felhasznalok.add(felhasznalo);
            }
        }
        model.addAttribute("users", felhasznalok);
        return "users";
    }

    @PostMapping(value = "/users/{email}")
    public String deleteUserHandler(
            @PathVariable("email") String email,
            @RequestParam(name = "indoklas", required = false) String indoklas
    ) {
        for (Felhasznalo felhasznalo : felhasznaloRepo.felhasznalokListaja()) {
            if (felhasznalo.getEmail().equals(email)) {
                felhasznaloRepo.delete(felhasznalo);
                emailKuldes.beregisztaltEmail(felhasznalo.getEmail(), "Kedves " + felhasznalo.getNev() + "!\n\nFiókodat töröltük a következő okok miatt:\n" + indoklas + "\n\nÜdvözlettel: A Szállás Neked csapata", "Felhasználó törölve");
                break;
            }
        }
        return "redirect:/users";
    }

    /*
     * Felh. adatainak módosítása
     * */
    @GetMapping(value = "/profil")
    public String profilEditHandler(Model model) {
        if (senkiNincsBejelentkezve) {
            return "log_reg";
        }
        //Menühüz kell
        model.addAttribute("berlo", berloVanBent);
        model.addAttribute("ures", senkiNincsBejelentkezve);
        model.addAttribute("kiado", kiadoVanBent);
        model.addAttribute("admin", adminVanBent);
        //idáig
        if (senkiNincsBejelentkezve) {
            return "log_reg";
        }
        model.addAttribute("felhasznalo", bejelentkezett);
        return "profil";
    }

    @PostMapping(value = "/profil_szerkeszt")
    public String profil_szerkeszt(
            @RequestParam(value = "name", defaultValue = "") String nev, @RequestParam(value = "psw", defaultValue = "") String jelszo,
            @RequestParam(value = "psw2", defaultValue = "") String jelszoMegegyszer, @RequestParam(value = "tel", defaultValue = "") String telefon,
            @RequestParam(value = "cim", defaultValue = "") String cim,
            RedirectAttributes redirectAttributes
    ) {
        String ujnev = bejelentkezett.getNev();
        String ujCim = bejelentkezett.getLakcim();
        String ujTelefon = bejelentkezett.getTelefonszam();
        String ujJelszo = bejelentkezett.getJelszo();
        if (nev.equals("")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a nevedet muszáj egy új nevet beírnod!");
            return "redirect:/profil";
        }
        if (cim.equals("")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a címedet muszáj egy új címet beírnod!");
            return "redirect:/profil";
        }
        if (telefon.equals("")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a telefonszámodat muszáj egy új számot beírnod!");
            return "redirect:/profil";
        }
        if (!jelszo.equals("")) {
            if (!jelszo.equals(jelszoMegegyszer)) {
                redirectAttributes.addFlashAttribute("Uzenet", "Nem egyeznek a megadott jelszavak!");
                return "redirect:/profil";
            } else {
                ujJelszo = jelszo;
            }
        }
        if (nev.equals(ujnev) && telefon.equals(ujTelefon) && cim.equals(ujCim) && ujJelszo.equals(bejelentkezett.getJelszo())) {
            redirectAttributes.addFlashAttribute("Uzenet", "Nem történt módosítás!");
            return "redirect:/profil";
        }
        if (!ujnev.equals(nev)) {
            ujnev = nev;
        }
        if (!ujCim.equals(cim)) {
            ujCim = cim;
        }
        if (!ujTelefon.equals(telefon)) {
            ujTelefon = telefon;
        }
        if (!ujJelszo.equals(jelszo) && !jelszo.equals("")) {
            ujJelszo = jelszo;
        }
        felhasznaloRepo.frissitesFelhasznalo(ujnev, ujTelefon, ujCim, ujJelszo, bejelentkezett.getEmail());
        bejelentkezett.setNev(ujnev);
        bejelentkezett.setTelefonszam(ujTelefon);
        bejelentkezett.setLakcim(ujCim);
        bejelentkezett.setJelszo(ujJelszo);
        redirectAttributes.addFlashAttribute("Uzenet", "Sikeres módosítás!");
        return "redirect:/profil";
    }

    /*
     * Felh. adatainak módosítása  VÉGE
     * */
    @RequestMapping(value = "/kilepes")
    public String kilepes(RedirectAttributes redirectAttributes) {
        berloVanBent = false;
        adminVanBent = false;
        kiadoVanBent = false;
        senkiNincsBejelentkezve = true;
        FoglalasController.getElozmenyek().clear();
        redirectAttributes.addFlashAttribute("Uzenet", "Sikeres kijelentkezés!");
        return "redirect:/log_reg";
    }

    // ------------------------------------------ Szallasok kezelese ---------------------------------------------------
    @GetMapping(value = {"/", "/index"})
    public String welcomeHandler(Model model) {
        //Menühüz kell
        model.addAttribute("berlo", berloVanBent);
        model.addAttribute("ures", senkiNincsBejelentkezve);
        model.addAttribute("kiado", kiadoVanBent);
        model.addAttribute("admin", adminVanBent);
        //idáig

        List<FoglalhatoSzallasok> szallasok_lista = foglalhatoSzallasokRepo.foglalhatoSzallasokListaja();

        // Szűrés növekvő, vagy csökkenő érték szerint
        if (Objects.equals(rendezesSzures, "") || Objects.equals(rendezesSzures, "novekvo")) {
            Comparator<FoglalhatoSzallasok> c = Comparator.comparing(FoglalhatoSzallasok::getArPerNapPerFo);
            szallasok_lista.sort(c);
        } else {
            Comparator<FoglalhatoSzallasok> c = Comparator.comparing(FoglalhatoSzallasok::getArPerNapPerFo);
            szallasok_lista.sort(c.reversed());
        }

        // Szűrés a település neve szerint
        if (!Objects.equals(telepulesSzures, "")) {
            List<FoglalhatoSzallasok> szallasok = new ArrayList<FoglalhatoSzallasok>();

            for (FoglalhatoSzallasok sz : szallasok_lista)
                if (sz.getHelyszin().toLowerCase().contains(telepulesSzures.toLowerCase()))
                    szallasok.add(sz);

            model.addAttribute("szallasok", szallasok);
        } else {
            model.addAttribute("szallasok", szallasok_lista);
        }

        model.addAttribute("szallasok2", foglalhatoSzallasokRepo.ajanlasokListaja());

        telepulesSzures = "";
        rendezesSzures = "";
        return "index";
    }

    @PostMapping(value = {"/", "/index"})
    public String welcomeSzallasListGet(
            @RequestParam("telepules") String telepules,
            @RequestParam("rendezes") String rendezes,
            Model model
    ) {
        telepulesSzures = telepules;
        rendezesSzures = rendezes;

        return "redirect:/index";
    }

    @GetMapping(value = "/szallas/{id}")
    public String szallas(
            @PathVariable("id") int id,
            Model model
    ) {
        //Menühüz kell
        model.addAttribute("berlo", berloVanBent);
        model.addAttribute("ures", senkiNincsBejelentkezve);
        model.addAttribute("kiadovanBent", kiadoVanBent);
        model.addAttribute("admin", adminVanBent);


        FoglalhatoSzallasok szallas = foglalhatoSzallasokRepo.findById(id).get();
        model.addAttribute("szallas", szallas);

        String cim = szallas.getHelyszin();
        System.out.println(cim);
        model.addAttribute("googleLink", googleMapService.getGoogleMapLink(cim));

        return "szallas";
    }

    @GetMapping(value = "/szallas/{id}/vissza")
    public String szallas(@PathVariable("id") int id) {
        return "redirect:/szallas/{id}";
    }
    /*
     * FOGLALÁS
     * */

    @GetMapping(value = "/szallas/{id}/foglal")
    public String foglal(
            @PathVariable("id") int id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (senkiNincsBejelentkezve) {
            redirectAttributes.addFlashAttribute("Uzenet", "Előbb kell jelentkezned, hogy foglalhass szállásokat!");
            return "redirect:/log_reg";
        }
        model.addAttribute("berlo", berloVanBent);
        model.addAttribute("ures", senkiNincsBejelentkezve);
        model.addAttribute("kiado", kiadoVanBent);
        model.addAttribute("admin", adminVanBent);
        for (FoglalhatoSzallasok sz : foglalhatoSzallasokRepo.foglalhatoSzallasokListaja())
            if (sz.getId() == id)
                model.addAttribute("szallas", sz);
        return "foglal";
    }

    @PostMapping(value = "/szallas/{id}/foglal")
    public String foglalSend(
            @PathVariable("id") int id,
            @RequestParam("date_start") String date_start,
            @RequestParam("date_end") String date_end,
            @RequestParam("dbszam") int dbszam,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        model.addAttribute("berlo", berloVanBent);
        model.addAttribute("ures", senkiNincsBejelentkezve);
        model.addAttribute("kiado", kiadoVanBent);
        model.addAttribute("admin", adminVanBent);

        bejelentkezett = UserController.getBejelentkezettFelhasznalo();
        LocalDate datum = LocalDate.now();
        LocalDate date1 = LocalDate.parse(date_start);
        LocalDate date2 = LocalDate.parse(date_end);

        if(date_start.equals(date_end)) {
            redirectAttributes.addFlashAttribute("Uzenet", "Nem adhatod meg ugyanazt a dátumot beköltözés és kiköltözés napjának, minimum 1 napra tudsz csak szállást bérelni!");
            return "redirect:/szallas/{id}/foglal";
        }
        if (Objects.equals(date_start, "") || Objects.equals(date_end, "")) {
            redirectAttributes.addFlashAttribute("Uzenet", "A dátumok megadása kötelező!");
            return "redirect:/szallas/{id}/foglal";
        } else if (datum.isAfter(date1) || datum.isEqual(date1)) {
            redirectAttributes.addFlashAttribute("Uzenet", "A foglalás kezdete nem lehet korábbi vagy a jelenlegi dátummal megegyező időpont!");
            return "redirect:/szallas/{id}/foglal";
        } else if (dbszam > foglalhatoSzallasokRepo.findById(id).get().getMaxFerohely()) {
            redirectAttributes.addFlashAttribute("Uzenet", "A maximális férőhely: " + foglalhatoSzallasokRepo.findById(id).get().getMaxFerohely() + "!");
            return "redirect:/szallas/{id}/foglal";
        } else if (date1.isAfter(date2)) {
            redirectAttributes.addFlashAttribute("Uzenet", "Nem lehet korábbi időpontra megadni a foglalás kezdetét, mint a végét!");
            return "redirect:/szallas/{id}/foglal";
        }
        Integer napok_szama = Math.toIntExact(ChronoUnit.DAYS.between(date1, date2));
        FoglalhatoSzallasok szallas = foglalhatoSzallasokRepo.findById(id).get();
        Integer osszeg = (foglalhatoSzallasokRepo.findById(id).get().getArPerNapPerFo() * dbszam) * napok_szama;
        Foglalas foglalas = new Foglalas(foglalhatoSzallasokRepo.findById(id).get(), bejelentkezett, datum, date1, date2, osszeg, dbszam, napok_szama);
        String szallasNeve = szallas.getNev();
        Integer ferohely = foglalasiElozmenyekRepo.szemelyekSzamaAzIdopontokKozott(date1, date2, szallas.getId());

        if (ferohely == null) {
            ferohely = 0;
        }
        Integer helyek = foglalasiElozmenyekRepo.modositandoSzemelyMennyisegMegadottDatumokKozott(date1, date2, foglalas.getId(), szallas.getId());
        if (helyek == null) {
            helyek = 0;
        }
        ferohely -= helyek;
        Integer marFoglalt = ferohely;
        int szemelyMaxMehet = szallas.getMaxFerohely() - ferohely;
        ferohely += dbszam;

        if (ferohely <= szallas.getMaxFerohely()) {
            FoglalasiElozmenyek foglalasElozmenyek = new FoglalasiElozmenyek(getBejelentkezettFelhasznalo(), datum, date1, date2, osszeg, dbszam, napok_szama, szallasNeve, id, "", szallas.getKiado().getEmail());
            foglalasRepo.save(foglalas);
            foglalasRepo.foglalasokListaja().add(foglalas);
            foglalasiElozmenyekRepo.save(foglalasElozmenyek);
            foglalasiElozmenyekRepo.szallasKepe(szallas.getKep(), foglalasElozmenyek.getId());
            foglalasiElozmenyekRepo.foglalasiElozmenyekListaja().add(foglalasElozmenyek);
            emailKuldes.beregisztaltEmail(bejelentkezett.getEmail(), "Sikeresen elmentettük a foglalásod a " + szallas.getNev() + "-hoz\n\nÖsszegzés:\n-Foglalás kezdete: " + foglalas.getKezdo_idopontja() + "\n-A szállást az alábbi napon el kell hagyni: " + date2 + "\n -Személyek száma: " + foglalas.getSzemelyMennyiseg() + "\n-Fizetendő összeg: " + osszeg + "\n Kiadó: " + szallas.getKiado().getNev() + "\n\nSzállás Neked csapata", "A foglalás sikeres!");
            emailKuldes.beregisztaltEmail(szallas.getKiado().getEmail(), "Foglalás történt a " + szallas.getNev() + "-ra\n\nFoglalás kezdete: " + date_start + " , Vége: " + date_end + "\nSzemélyek száma: " + dbszam + "\n\nBérlő adatai:\n Neve: " + bejelentkezett.getNev() + "\nE-mail címe: " + bejelentkezett.getEmail() + "\nTelefonszáma: " + bejelentkezett.getTelefonszam() + "\n\nSzállás Neked csapata", "Foglalás értesítés");
        } else {
            boolean talaltJoDatumotElore = false;
            LocalDate ujKezdoElore = date1;
            LocalDate ujVegzoElore = date2;
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
                ferohely += dbszam;
                if (ferohely <= szallas.getMaxFerohely()) {
                    talaltJoDatumotElore = true;
                }
            }

            boolean talaltJoDatumotHatra = false;
            boolean tudHatra = true;
            LocalDate ujKezdoHatra = date1;
            LocalDate ujVegzoHatra = date2;
            while (!talaltJoDatumotHatra) {
                ujKezdoHatra = ujKezdoHatra.minusDays(1);
                ujVegzoHatra = ujVegzoHatra.minusDays(1);
                if (ujKezdoHatra.isEqual(datum)) {
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
                ferohely += dbszam;
                if (ferohely <= szallas.getMaxFerohely()) {
                    talaltJoDatumotHatra = true;
                }
            }
            if (szemelyMaxMehet <= 0) {
                if (tudHatra) {
                    redirectAttributes.addFlashAttribute("Uzenet", "Sajnos erre az időszakra már minden helyet lefoglaltak a szálláson! A Legközelebbi szabad időpont a kívánt időponthoz képest " + napok_szama + " napra " + dbszam + " főnek " + ujKezdoElore + "-tól, vagy ha előbb szeretnél akkor már " + ujKezdoHatra + "-tól lehetséges!");

                }
                if (!tudHatra) {
                    redirectAttributes.addFlashAttribute("Uzenet", "Sajnos erre az időszakra már minden helyet lefoglaltak a szálláson! A Legközelebbi szabad időpont a kívánt időponthoz képest " + napok_szama + " napra " + dbszam + " főnek " + ujKezdoElore + "-tól lehetséges, előbb már sajnos nincs lehetőség!");

                }
            } else {
                if (tudHatra) {
                    redirectAttributes.addFlashAttribute("Uzenet", "Sajnos erre az időszakra már csak " + szemelyMaxMehet + " személynek foglalható ez a szállás, mert a többi " + marFoglalt + " helyet már lefoglalták! A Legközelebbi szabad időpont a kívánt időponthoz képest " + napok_szama + " napra " + dbszam + " főnek " + ujKezdoElore + "-tól, vagy ha előbb szeretnél akkor már " + ujKezdoHatra + "-tól lehetséges!");

                }
                if (!tudHatra) {
                    redirectAttributes.addFlashAttribute("Uzenet", "Sajnos erre az időszakra már csak " + szemelyMaxMehet + " személynek foglalható ez a szállás, mert a többi " + marFoglalt + " helyet már lefoglalták! A Legközelebbi szabad időpont a kívánt időponthoz képest " + napok_szama + " napra " + dbszam + " főnek " + ujKezdoElore + "-tól lehetséges, előbb már sajnos nincs lehetőség!");

                }
            }
            return "redirect:/szallas/{id}/foglal";
        }

        redirectAttributes.addAttribute("foglalas_id", foglalas.getId());

        return "redirect:/szallas/{id}/foglal/foglalas_nyugta/{foglalas_id}";
    }

    @GetMapping(value = "/szallas/{id}/foglal/foglalas_nyugta/{foglalas_id}")
    public String foglalasNyugta(
            @PathVariable("id") int szallas_id,
            @PathVariable("foglalas_id") int foglalas_id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if(bejelentkezett == null) {
            return "redirect:/log_reg";
        }
        model.addAttribute("berlo", berloVanBent);
        model.addAttribute("ures", senkiNincsBejelentkezve);
        model.addAttribute("kiado", kiadoVanBent);
        model.addAttribute("admin", adminVanBent);
        if (senkiNincsBejelentkezve) {
            return "redirect:/log_reg";
        }
        bejelentkezett = UserController.getBejelentkezettFelhasznalo();


        FoglalhatoSzallasok jelenlegi_szallas = foglalhatoSzallasokRepo.findById(szallas_id).get();
        FoglalasiElozmenyek jelenlegi_foglalas = foglalasiElozmenyekRepo.findById(foglalas_id).get();

        model.addAttribute("szallas_nyugta", jelenlegi_szallas);
        model.addAttribute("foglalas_nyugta", jelenlegi_foglalas);


        return "foglalas_nyugta";
    }

    /*
     * Admin funckiók
     *
     *
     * */
    @GetMapping(value = "/szallasok_admin")
    public String szallasAdminHandler(Model model) {
        if (senkiNincsBejelentkezve) {
            return "log_reg";
        }
        model.addAttribute("szallasok", foglalhatoSzallasokRepo.foglalhatoSzallasokListaja());

        return "szallasok_admin";
    }

    @PostMapping(value = "/szallasok_admin/{id}")
    public String szallasDelete(
            @PathVariable("id") Integer id,
            @RequestParam(name = "indoklas", required = false) String indoklas
    ) {
        if (senkiNincsBejelentkezve) {
            return "log_reg";
        }
        System.out.println(id + " törölve: " + indoklas);

        for (FoglalhatoSzallasok szallas : foglalhatoSzallasokRepo.foglalhatoSzallasokListaja()) {
            if (szallas.getId() == id) {
                foglalhatoSzallasokRepo.delete(szallas);
                emailKuldes.beregisztaltEmail(szallas.getKiado().getEmail(), "Kedves " + szallas.getKiado().getNev() + "!\n\nAz alábbi szállásodat töröltük:\n\nNév: " + szallas.getNev() + "\nHelyszín: " + szallas.getHelyszin() + "\nLeírás:\n" + szallas.getLeiras() + "\n\nTörlés indoklása:\n" + indoklas, "Szállás törölve admin által");
                break;
            }
        }

        return "redirect:/szallasok_admin";
    }

    @GetMapping(value = "/hirdeteseim")
    public String hirdeteseimHandler(Model model) {
        if (senkiNincsBejelentkezve) {
            return "log_reg";
        }
        List<FoglalhatoSzallasok> osszes_szallas = foglalhatoSzallasokRepo.foglalhatoSzallasokListaja();
        List<FoglalhatoSzallasok> hirdeteseim = new ArrayList<FoglalhatoSzallasok>();

        for (FoglalhatoSzallasok f : osszes_szallas) {
            if (Objects.equals(f.getKiado().getEmail(), bejelentkezett.getEmail())) {
                hirdeteseim.add(f);
            }
        }

        model.addAttribute("szallasok", hirdeteseim);
        model.addAttribute("vanKep", vanKep);
        //model.addAttribute("szallasok", foglalhatoSzallasokRepo.foglalhatoSzallasokListaja() );

        return "hirdeteseim";
    }

    @GetMapping(value = "/hirdeteseim/{id}/szerkeszt")
    public String hirdetesem(
            @PathVariable("id") int id,
            Model model
    ) {
        if (senkiNincsBejelentkezve) {
            return "redirect:/log_reg";
        }
        FoglalhatoSzallasok szallas = foglalhatoSzallasokRepo.findById(id).get();
        model.addAttribute("szallas", szallas);

        return "hirdetes_szerkesztes";
    }

    @PostMapping(value = "/hirdeteseim/{id}/szerkesztes")
    public String hirdetesUjImg(
            @PathVariable("id") int id, @RequestParam(name = "nev", required = false) String nev, @RequestParam(name = "cim", required = false) String cim,
            @RequestParam(name = "ar", required = false) Integer ar, @RequestParam(name = "ferohely", required = false) Integer ferohely,
            @RequestParam(name = "leiras", required = false) String leiras,
            @RequestParam(name = "img", required = false) MultipartFile img, RedirectAttributes redirectAttributes
    ) {
        FoglalhatoSzallasok szallasok = foglalhatoSzallasokRepo.findById(id).get();
        String ujnev = szallasok.getNev();
        String ujcim = szallasok.getHelyszin();
        Integer ujAr = szallasok.getArPerNapPerFo();
        Integer ujFerohely = szallasok.getMaxFerohely();
        String ujLeiras = szallasok.getLeiras();
        if (nev.equals("")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a szállásod nevét muszáj egy új nevet beírnod!");
            return "redirect:/hirdeteseim/{id}/szerkeszt";
        }
        if (cim.equals("")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a szállásod címét muszáj egy új címet beírnod!");
            return "redirect:/hirdeteseim/{id}/szerkeszt";
        }
        if (ar == null) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a szállásod árát muszáj egy új árat beírnod!");
            return "redirect:/hirdeteseim/{id}/szerkeszt";
        }
        if (ferohely == null) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a szállásod kapacitását muszáj egy új férőhely számot beírnod!");
            return "redirect:/hirdeteseim/{id}/szerkeszt";
        }
        if (leiras.equals("")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a szállásod leírását muszáj valamit beírnod!");
            return "redirect:/hirdeteseim/{id}/szerkeszt";
        }
        if (!ujnev.equals(nev)) {
            ujnev = nev;
        }
        if (!ujcim.equals(cim)) {
            ujcim = cim;
        }
        if (!Objects.equals(ujAr, ar)) {
            ujAr = ar;
        }
        if (!Objects.equals(ujFerohely, ferohely)) {
            ujFerohely = ferohely;
        }
        if (!ujLeiras.equals(leiras)) {
            ujLeiras = leiras;
        }


        if (!szallasok.getKep().equals("")) {
            regiKep = szallasok.getKep();
        }

        foglalhatoSzallasokRepo.frissitesSzallasAlap(ujAr, ujcim, ujLeiras, ujFerohely, ujnev, id);
        String modositandoKep = "";
        if (img != null) {
            String kepNeve = StringUtils.cleanPath(Objects.requireNonNull(img.getOriginalFilename()));
            if (kepNeve.contains("..")) {
                redirectAttributes.addFlashAttribute("Uzenet", "Érvénytelen képformátum!");
                return "redirect:/hirdeteseim/{id}/szerkeszt";
            }
        }
        try {
            assert img != null;
            float kepMerete = img.getSize();
            if (kepMerete <= 800000) {
                if (img != null) {
                    modositandoKep = Base64.getEncoder().encodeToString(img.getBytes());
                }
                if (modositandoKep.equals("") && regiKep.equals("")) {
                    foglalhatoSzallasokRepo.frissitesSzallasKepe(modositandoKep, id);
                }
                if (!modositandoKep.equals("")) {
                    foglalhatoSzallasokRepo.frissitesSzallasKepe(modositandoKep, id);
                }
                if (!regiKep.equals("") && regiKep.equals(modositandoKep)) {
                    foglalhatoSzallasokRepo.frissitesSzallasKepe(regiKep, id);
                }
                redirectAttributes.addFlashAttribute("Uzenet", "Sikeres módosítás!");
                return "redirect:/hirdeteseim/{id}/szerkeszt";
            } else {
                redirectAttributes.addFlashAttribute("Uzenet", "Túl nagy méretű képet próbálsz meg feltölteni, a max méret 0,8Mb! Az általad feltöltött kép mérete: " + kepMerete / 1000000 + "Mb");
                return "redirect:/hirdeteseim/{id}/szerkeszt";
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("Uzenet", "Váratlan hiba történt, kérjük próbáld újra!");
            return "redirect:/hirdeteseim/{id}/szerkeszt";
        }
    }

    @PostMapping(value = "/hirdeteseim/{id}/kep_torlese")
    public String kepTorlese(@PathVariable("id") int id, @RequestParam(name = "img", required = false) MultipartFile img, RedirectAttributes redirectAttributes) {
        String modositandoKep = "";
        if (img != null) {
            String kepNeve = StringUtils.cleanPath(Objects.requireNonNull(img.getOriginalFilename()));
            if (kepNeve.contains("..")) {
                redirectAttributes.addFlashAttribute("Uzenet", "Érvénytelen képformátum!");
                return "redirect:/hirdeteseim/{id}/szerkeszt";
            }
        }
        regiKep = "";
        foglalhatoSzallasokRepo.frissitesSzallasKepe(regiKep, id);
        redirectAttributes.addFlashAttribute("Uzenet", "Sikeres képeltávolítás");
        return "redirect:/hirdeteseim/{id}/szerkeszt";

    }

    @GetMapping(value = "/kiado_uj_hirdetes")
    public String ujSzallasLap() {
        if (senkiNincsBejelentkezve) {
            return "redirect:/log_reg";
        }
        return "kiado_uj_hirdetes";
    }

    @PostMapping(value = "/kiado_uj_hirdetes")
    public String ujSzallas(
            @RequestParam("name") String name,
            @RequestParam("cim") String cim,
            @RequestParam("ar") String ar,
            @RequestParam("ferohely") Integer ferohely,
            @RequestParam("leiras") String leiras,
            @RequestParam(name = "img", required = false) MultipartFile img, RedirectAttributes redirectAttributes
    ) {
        if (senkiNincsBejelentkezve) {
            return "redirect:/log_reg";
        }
        assert img != null;
        double kepMerete = img.getSize();
        if (kepMerete > 800000) {
            redirectAttributes.addFlashAttribute("Uzenet", "Túl nagy méretű képet próbálsz meg feltölteni, a max méret 0,8Mb! Az általad feltöltött kép mérete: " + kepMerete / 1000000 + "Mb");
            return "redirect:/kiado_uj_hirdetes";
        }
        String kepNeve = StringUtils.cleanPath(Objects.requireNonNull(img.getOriginalFilename()));
        String kep = "";
        if (kepNeve.contains("..")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Érvénytelen képformátum!");
            return "redirect:/kiado_uj_hirdetes";
        }
        try {
            kep = Base64.getEncoder().encodeToString(img.getBytes());
            FoglalhatoSzallasok uj_szallas = new FoglalhatoSzallasok(name, bejelentkezett, cim, leiras, Integer.parseInt(ar), ferohely, "");
            foglalhatoSzallasokRepo.save(uj_szallas);
            foglalhatoSzallasokRepo.foglalhatoSzallasokListaja().add(uj_szallas);
            foglalhatoSzallasokRepo.frissitesSzallasKepe(kep, uj_szallas.getId());
            redirectAttributes.addFlashAttribute("Uzenet", "Sikeresen létrehoztad az új " + uj_szallas.getNev() + " hírdetésedet!");
            return "redirect:/hirdeteseim";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("Uzenet", "Váratlan hiba történt, kérjük próbáld újra!");
            return "redirect:/kiado_uj_hirdetes";
        }
    }

    @GetMapping(value = "szallas_torlese")
    public String szallas_torlese(@RequestParam("szallasAzonosito") Integer id, RedirectAttributes redirectAttributes) {

        LocalDate datum = LocalDate.now();
        FoglalhatoSzallasok szallas = foglalhatoSzallasokRepo.findById(id).get();
        for (FoglalasiElozmenyek elozmenyek : foglalasiElozmenyekRepo.foglalasiElozmenyekListaja()) {
            if ((elozmenyek.getSzallasIDElozmeny().equals(szallas.getId()) && elozmenyek.getKezdo_idopontja().isEqual(datum)) || elozmenyek.getSzallasIDElozmeny().equals(szallas.getId()) && elozmenyek.getKezdo_idopontja().isAfter(datum)) {
                emailKuldes.beregisztaltEmail(elozmenyek.getBejelentkezettFelhasznalo().getEmail(), "Ezúton értesítünk, hogy sajnos a " + szallas.getNev() + "hoz tartozó foglalásodat lemondták a kiadó által.\nA kellemetlenségért elnézést kérünk.\n\nÜdvözlettel: A Szállás Neked csapata", "Szállás lemondásra került");
            }
        }
        foglalhatoSzallasokRepo.deleteById(id);
        redirectAttributes.addFlashAttribute("Uzenet", "Sikeresen törölted a hírdetésedet!");
        return "redirect:/hirdeteseim";
    }
}
