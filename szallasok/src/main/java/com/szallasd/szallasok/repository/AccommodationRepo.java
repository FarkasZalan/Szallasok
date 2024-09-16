package com.szallasd.szallasok.repository;

import com.szallasd.szallasok.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface AccommodationRepo extends JpaRepository<Accommodation, Integer> {
    // Retrieves all accommodations from the database
    @Query(value = "select * from accommodation", nativeQuery = true)
    List<Accommodation> listOfTheAccommodation();

    // Retrieves the top 3 accommodations sorted by price per day per person
    @Query(value = "select * from accommodation order by price_per_day_per_person limit 3", nativeQuery = true)
    List<Accommodation> listOfRecommendations();

    // Updates the details of specific accommodation identified by accommodationID
    // Parameters include price per day per person, location, description, maximum capacity, and name
    @Modifying
    @Query(value="update accommodation set price_per_day_per_person= :price_per_day_per_person, location= :location, description= :description, max_capacity= :max_capacity, name= :name where accommodationID = :id", nativeQuery = true)
    void update(@Param("price_per_day_per_person") Integer price_per_day_per_person,
                @Param("location") String location,
                @Param("description") String description,
                @Param("max_capacity") Integer max_capacity,
                @Param("name") String name,
                @Param("id") Integer id
    );

    // Updates the image for specific accommodation identified by accommodationID
    @Modifying
    @Query(value="update accommodation set image= :kep where accommodationID = :id", nativeQuery = true)
    void updateImage(@Param("image") String image,
                     @Param("id") Integer id
    );
}
