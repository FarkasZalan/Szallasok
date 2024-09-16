package com.szallasd.szallasok.service;


import com.szallasd.szallasok.entity.BookingHistory;
import com.szallasd.szallasok.entity.User;
import com.szallasd.szallasok.repository.BookingHistoryRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

public class UserBookings {
    // for the @Autowired warning
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private final BookingHistoryRepo bookingHistoryRepo;

    public UserBookings(BookingHistoryRepo bookingHistoryRepo) {
        this.bookingHistoryRepo = bookingHistoryRepo;
    }

    //
    public List<BookingHistory> listOfTheUserBookings(List<BookingHistory> bookingList, User loggedUser) {
        // Initialize a flag to track whether a booking is already in the list
        boolean alreadyInTheList = false;

        // Loop through all the booking histories retrieved from the repository
        for (BookingHistory history : bookingHistoryRepo.listOfBookingHistory()) {

            // Check if the booking history belongs to the currently logged-in user by comparing email addresses
            if (history.getLoggedInUser().getEmail().equals(loggedUser.getEmail())) {

                // Loop through the existing bookings in the 'bookingList' to check if this booking is already present
                for (BookingHistory bookingListElement : bookingList) {

                    // If the booking is already in the list (matching IDs), set the flag to true and break out of the loop
                    if (Objects.equals(bookingListElement.getId(), history.getId())) {
                        alreadyInTheList = true;
                        break; // No need to check further once the booking is found
                    }
                    // If no match is found, set the flag to false
                    alreadyInTheList = false;
                }

                // If the booking is not already in the list, add it to 'bookingList'
                if (!alreadyInTheList) {
                    bookingList.add(history);
                }
            }
        }
        // Return the updated list of bookings for the logged-in user
        return bookingList;
    }
}
