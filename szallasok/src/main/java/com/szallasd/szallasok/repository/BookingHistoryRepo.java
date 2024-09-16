package com.szallasd.szallasok.repository;

import com.szallasd.szallasok.entity.BookingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Transactional
public interface BookingHistoryRepo extends JpaRepository<BookingHistory, Integer> {
    // Retrieves all booking history records from the database
    @Query(value = "select * from booking_history", nativeQuery = true)
    List<BookingHistory> listOfBookingHistory();

    // Updates the booking history record with the specified ID
    // This includes fields like total amount, start date, end date, number of days, and number of people
    @Modifying
    @Query(value="update booking_history set total_amount= :total_amount, booking_start_date= :booking_start_date, day_quantity= :day_quantity, person_quantity= :person_quantity, booking_emd_date= :booking_emd_date where booking_history_id = :id", nativeQuery = true)
    void updateBookingHistory(@Param("booking_start_date") LocalDate booking_start_date,
                              @Param("booking_emd_date") LocalDate booking_emd_date,
                              @Param("total_amount") Integer total_amount,
                              @Param("day_quantity") Integer day_quantity,
                              @Param("person_quantity") Integer person_quantity,
                              @Param("id") Integer id
    );

    // Returns the total number of people who have already booked the specified accommodation
    // within the given date range. This helps in determining available capacity
    @Query(value="SELECT SUM(`person_quantity`) FROM booking_history WHERE accommodationID = :id AND ((`booking_start_date` BETWEEN :booking_start_date AND :booking_emd_date) OR (`booking_emd_date` BETWEEN :booking_start_date AND :booking_emd_date) OR (`booking_start_date` <= :booking_start_date AND `booking_emd_date` >= :booking_emd_date));", nativeQuery = true)
    Integer getAvailableCapacityBetweenDates(@Param("booking_start_date") LocalDate booking_start_date,
                                             @Param("booking_emd_date") LocalDate booking_emd_date,
                                             @Param("id") Integer id
    );

    // Calculates the total number of people for a specific booking within the given date range
    // This helps to ensure that the modification of a booking does not exceed the accommodation's capacity
    @Query(value="SELECT SUM(`person_quantity`) FROM booking_history WHERE booking_history_id = :id AND accommodationID = :accommodationId AND ((`booking_start_date` BETWEEN :booking_start_date AND :booking_emd_date) OR (`booking_emd_date` BETWEEN :booking_start_date AND :booking_emd_date) OR (`booking_start_date` <= :booking_start_date AND `booking_emd_date` >= :booking_emd_date))", nativeQuery = true)
    Integer quantityOfPersonToBeModifiedBetweenGivenDates(@Param("booking_start_date") LocalDate booking_start_date,
                                                          @Param("booking_emd_date") LocalDate booking_emd_date,
                                                          @Param("id") Integer id,
                                                          @Param("accommodationId") Integer accommodationId
    );

    // Updates the image for a specific booking history record identified by booking_history_id
    @Modifying
    @Query(value="update booking_history set accommodation_image= :accommodation_image where booking_history_id = :booking_history_id", nativeQuery = true)
    void updateAccommodationImage(@Param("accommodation_image") String accommodation_image,
                                  @Param("booking_history_id") Integer id
    );
}
