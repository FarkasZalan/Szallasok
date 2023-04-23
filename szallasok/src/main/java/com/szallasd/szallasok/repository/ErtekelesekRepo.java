package com.szallasd.szallasok.repository;

import com.szallasd.szallasok.model.Ertekelesek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErtekelesekRepo extends JpaRepository<Ertekelesek, Integer> {
    @Query(value = "select * from ertekelesek", nativeQuery = true)
    List<Ertekelesek> ertekelesekListaja();
}
