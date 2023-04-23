package com.szallasd.szallasok.repository;

import com.szallasd.szallasok.model.Foglalas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Transactional
public interface FoglalasRepo extends JpaRepository<Foglalas, Integer> {
    @Query(value = "select * from foglalas", nativeQuery = true)
    List<Foglalas> foglalasokListaja();

    @Modifying
    @Query(value="update foglalas set fizetendo_osszeg= :fizetendo_osszeg, kezdo_idopontja= :kezdo_idopontja, nap_mennyiseg= :nap_mennyiseg, szemely_mennyiseg= :szemely_mennyiseg, vegzo_idopontja= :vegzo_idopontja where foglalasid = :id", nativeQuery = true)
    void frissitesFoglalas(@Param("kezdo_idopontja") LocalDate kezdo_idopontja, @Param("vegzo_idopontja") LocalDate vegzo_idopontja, @Param("fizetendo_osszeg") Integer fizetendo_osszeg, @Param("nap_mennyiseg") Integer nap_mennyiseg, @Param("szemely_mennyiseg") Integer szemely_mennyiseg,
                               @Param("id") Integer id);
}
