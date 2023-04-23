package com.szallasd.szallasok.repository;

import com.szallasd.szallasok.model.Felhasznalo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Transactional
public interface FelhasznaloRepo extends JpaRepository<Felhasznalo, String> {
    @Query(value = "select * from felhasznalo", nativeQuery = true)
    List<Felhasznalo> felhasznalokListaja();

    @Modifying
    @Query(value="update felhasznalo set nev= :nev, telefonszam= :telefonszam, lakcim= :lakcim, jelszo= :jelszo where email = :email", nativeQuery = true)
    void frissitesFelhasznalo(@Param("nev") String nev, @Param("telefonszam") String telefonszam, @Param("lakcim") String lakcim, @Param("jelszo") String jelszo, @Param("email") String email);
}
