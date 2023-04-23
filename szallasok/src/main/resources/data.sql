--Alapértelmezett felhasználók
INSERT  into felhasznalo (email,felhasznalo_tipus, nev, lakcim, telefonszam, jelszo) VALUES ('admin','Admin', 'Admin', 'SZTE Irinyi épület', '+36 30 000 0000','admin')
ON DUPLICATE KEY UPDATE email = VALUES (email),felhasznalo_tipus = VALUES (felhasznalo_tipus), nev = VALUES(nev), lakcim = VALUES(lakcim), telefonszam = VALUES(telefonszam), jelszo = VALUES(jelszo);
INSERT  into felhasznalo (email,felhasznalo_tipus, nev, lakcim, telefonszam, jelszo) VALUES ('farkaszalan2001@gmail.com','Bérlő', 'Farkas Zalán', 'Szeged', '+36 30 000 0000','123')
ON DUPLICATE KEY UPDATE email = VALUES (email),felhasznalo_tipus = VALUES (felhasznalo_tipus), nev = VALUES(nev), lakcim = VALUES(lakcim), telefonszam = VALUES(telefonszam), jelszo = VALUES(jelszo);

INSERT  into foglalhato_szallasok (szallasID,nev, kiado_email, helyszin, leiras, ar_per_nap_per_fo, max_ferohely,kep) VALUES (1,'Balatoni szállás','farkaszalan2001@gmail.com', 'Siófok, egyutca 1', 'Szép kilátás a balatonra', 1200, 12, '')
ON DUPLICATE KEY UPDATE szallasID = VALUES (szallasID), nev = VALUES (nev),kiado_email = VALUES (kiado_email), helyszin = VALUES(helyszin), leiras = VALUES(leiras), ar_per_nap_per_fo = VALUES(ar_per_nap_per_fo), max_ferohely = VALUES(max_ferohely), kep = VALUES(kep);