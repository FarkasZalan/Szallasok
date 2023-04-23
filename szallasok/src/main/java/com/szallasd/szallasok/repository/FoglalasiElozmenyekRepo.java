package com.szallasd.szallasok.repository;

import com.szallasd.szallasok.model.FoglalasiElozmenyek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Transactional
public interface FoglalasiElozmenyekRepo extends JpaRepository<FoglalasiElozmenyek, Integer> {
    @Query(value = "select * from foglalasi_elozmenyek", nativeQuery = true)
    List<FoglalasiElozmenyek> foglalasiElozmenyekListaja();

    @Modifying
    @Query(value="update foglalasi_elozmenyek set fizetendo_osszeg= :fizetendo_osszeg, kezdo_idopontja= :kezdo_idopontja, nap_mennyiseg= :nap_mennyiseg, szemely_mennyiseg= :szemely_mennyiseg, vegzo_idopont= :vegzo_idopont where foglalasi_elozmenyid = :id", nativeQuery = true)
    void frissitesFoglalasElozmeny(@Param("kezdo_idopontja") LocalDate kezdo_idopontja,@Param("vegzo_idopont") LocalDate vegzo_idopont,@Param("fizetendo_osszeg") Integer fizetendo_osszeg, @Param("nap_mennyiseg") Integer nap_mennyiseg, @Param("szemely_mennyiseg") Integer szemely_mennyiseg,
                               @Param("id") Integer id);

    @Query(value="SELECT SUM(`szemely_mennyiseg`) FROM foglalasi_elozmenyek WHERE szallas_id = :id AND ((`kezdo_idopontja` BETWEEN :kezdo_idopontja AND :vegzo_idopont) OR (`vegzo_idopont` BETWEEN :kezdo_idopontja AND :vegzo_idopont) OR (`kezdo_idopontja` <= :kezdo_idopontja AND `vegzo_idopont` >= :vegzo_idopont));", nativeQuery = true)
    Integer szemelyekSzamaAzIdopontokKozott(LocalDate kezdo_idopontja,LocalDate vegzo_idopont, @Param("id") Integer id);

    @Query(value="SELECT SUM(`szemely_mennyiseg`) FROM foglalasi_elozmenyek WHERE foglalasi_elozmenyid = :id AND szallas_id = :szallasId AND ((`kezdo_idopontja` BETWEEN :kezdo_idopontja AND :vegzo_idopont) OR (`vegzo_idopont` BETWEEN :kezdo_idopontja AND :vegzo_idopont) OR (`kezdo_idopontja` <= :kezdo_idopontja AND `vegzo_idopont` >= :vegzo_idopont))", nativeQuery = true)
    Integer modositandoSzemelyMennyisegMegadottDatumokKozott(LocalDate kezdo_idopontja,LocalDate vegzo_idopont, @Param("id") Integer id, @Param("szallasId") Integer szallasId);

    @Modifying
    @Query(value="update foglalasi_elozmenyek set szallas_kepe= :szallas_kepe where foglalasi_elozmenyid = :foglalasi_elozmenyid", nativeQuery = true)
    void szallasKepe(@Param("szallas_kepe") String szallas_kepe, @Param("foglalasi_elozmenyid") Integer id);
}
