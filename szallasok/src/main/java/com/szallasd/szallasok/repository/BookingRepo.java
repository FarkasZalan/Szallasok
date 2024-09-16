package com.szallasd.szallasok.repository;

import com.szallasd.szallasok.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Transactional
public interface BookingRepo extends JpaRepository<Booking, Integer> {
    // Retrieves all bookings from the database.
    @Query(value = "select * from booking", nativeQuery = true)
    List<Booking> listOfTheBookings();

    // Updates the details of a specific booking identified by bookingID.
    // Parameters include total amount, starting date, ending date, number of days, and number of people.
    @Modifying
    @Query(value="update booking set total_amount= :total_amount, starting_date= :starting_date, day_quantity= :day_quantity, person_quantity= :person_quantity, ending_date= :ending_date where bookingID = :id", nativeQuery = true)
    void updateBooking(@Param("starting_date") LocalDate starting_date,
                       @Param("ending_date") LocalDate ending_date,
                       @Param("total_amount") Integer total_amount,
                       @Param("day_quantity") Integer day_quantity,
                       @Param("person_quantity") Integer person_quantity,
                       @Param("id") Integer id
    );
}
