package com.szallasd.szallasok.repository;

import com.szallasd.szallasok.entity.Evaluations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationsRepo extends JpaRepository<Evaluations, Integer> {
    // Retrieves all viewReviews from the database.
    @Query(value = "select * from evaluations", nativeQuery = true)
    List<Evaluations> listOfTheEvaluations();
}
