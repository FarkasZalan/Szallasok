package com.szallasd.szallasok.repository;

import com.szallasd.szallasok.model.FoglalhatoSzallasok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Transactional
public interface FoglalhatoSzallasokRepo extends JpaRepository<FoglalhatoSzallasok, Integer> {
    @Query(value = "select * from foglalhato_szallasok", nativeQuery = true)
    List<FoglalhatoSzallasok> foglalhatoSzallasokListaja();

    @Query(value = "select * from foglalhato_szallasok order by ar_per_nap_per_fo limit 3", nativeQuery = true)
    List<FoglalhatoSzallasok> ajanlasokListaja();

    @Modifying
    @Query(value="update foglalhato_szallasok set ar_per_nap_per_fo= :ar_per_nap_per_fo, helyszin= :helyszin, leiras= :leiras, max_ferohely= :max_ferohely, nev= :nev where szallasid = :id", nativeQuery = true)
    void frissitesSzallasAlap(@Param("ar_per_nap_per_fo") Integer ar_per_nap_per_fo, @Param("helyszin") String helyszin, @Param("leiras") String leiras, @Param("max_ferohely") Integer max_ferohely,@Param("nev") String nev,
                                   @Param("id") Integer id);

    @Modifying
    @Query(value="update foglalhato_szallasok set kep= :kep where szallasid = :id", nativeQuery = true)
    void frissitesSzallasKepe(@Param("kep") String kep, @Param("id") Integer id);
}
