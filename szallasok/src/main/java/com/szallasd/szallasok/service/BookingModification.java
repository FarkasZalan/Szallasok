package com.szallasd.szallasok.service;

import com.szallasd.szallasok.controller.UserController;
import com.szallasd.szallasok.entity.Accommodation;
import com.szallasd.szallasok.entity.User;
import com.szallasd.szallasok.entity.BookingHistory;
import com.szallasd.szallasok.repository.AccommodationRepo;
import com.szallasd.szallasok.repository.BookingRepo;
import com.szallasd.szallasok.repository.BookingHistoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Service class for managing and modifying bookings.
 * This class handles the logic for updating bookings, including validation of dates,
 * capacities, and email notifications.
 */
@Service
public class BookingModification {
    private final BookingHistoryRepo bookingHistoryRepo;
    private final AccommodationRepo accommodationRepo;
    private final BookingRepo bookingRepo;

    // for the @Autowired warning
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmailSend emailSend;
    public BookingModification(EmailSend emailSend, BookingHistoryRepo bookingHistoryRepo, AccommodationRepo accommodationRepo, BookingRepo bookingRepo) {
        this.emailSend = emailSend;
        this.bookingHistoryRepo = bookingHistoryRepo;
        this.accommodationRepo = accommodationRepo;
        this.bookingRepo = bookingRepo;
    }

    /**
     * Modifies an existing booking with new dates and person quantity.
     *
     * @param booking The booking to be modified.
     * @param startDate The new start date for the booking.
     * @param endDate The new end date for the booking.
     * @param personQuantity The new number of people for the booking.
     * @return A message indicating the result of the modification.
     */
    public String modifyBooking(BookingHistory booking, LocalDate startDate, LocalDate endDate, Integer personQuantity) {
        // Retrieve the accommodation entity
        Accommodation accommodation = accommodationRepo.findById(booking.getAccommodationId()).orElse(null);
        if (accommodation == null) {
            return "Szállás nem található.";
        }

        // Validate the booking modification
        String validationMessage = validateBookingModification(startDate, endDate, personQuantity, booking, accommodation);
        if (!validationMessage.isEmpty()) {
            return validationMessage;
        }

        // Update the booking if validation passes
        updateBooking(booking, startDate, endDate, personQuantity, accommodation);
        return "Sikeres módosítás!";
    }

    private String validateBookingModification(LocalDate startDate, LocalDate endDate, Integer personQuantity, BookingHistory booking, Accommodation accommodation) {
        LocalDate today = LocalDate.now();

        // Find the error source in the parameters

        if (startDate.isEqual(endDate)) {
            return "Nem adhatod meg ugyanazt a dátumot beköltözés és kiköltözés napjának, minimum 1 napra tudsz csak szállást bérelni!";
        }

        if (startDate.isEqual(today)) {
            return "Nem adhatod meg a mai napot a beköltözés napjának, mindig csak következő naptól tudsz foglalni ha még van hely a szálláson!";
        }

        if (startDate.isAfter(endDate)) {
            return "Nem lehet későbbre tenni az érkezés dátumát mint a kiköltözés dátuma!";
        }

        if (startDate.isBefore(today) || endDate.isBefore(today)) {
            return "Nem lehet a mai dátumnál korábbra foglalni!";
        }

        if (personQuantity > accommodation.getMaxCapacity()) {
            return "A megadott személyek száma meghaladja a szállás maxiumum " + accommodation.getMaxCapacity() + " fős kapacitását!";
        }

        // Check for availability
        if (!isAccommodationAvailable(startDate, endDate, booking.getId(), accommodation, personQuantity)) {
            return "A szállás nem elérhető a kiválasztott dátumokra és személyszámra.";
        }

        return "";
    }

    /**
     * Checks if the accommodation is available for the specified dates and number of people.
     *
     * @param startDate The start date of the booking.
     * @param endDate The end date of the booking.
     * @param bookingId The ID of the booking to be modified (if any).
     * @param accommodation The accommodation to check availability for.
     * @param personQuantity The number of people for the booking.
     * @return True if the accommodation is available, otherwise false.
     */
    private boolean isAccommodationAvailable(LocalDate startDate, LocalDate endDate, Integer bookingId,  Accommodation accommodation, Integer personQuantity) {
        // Get the total available capacity of the accommodation between the specified dates
        Integer availableCapacity = bookingHistoryRepo.getAvailableCapacityBetweenDates(startDate, endDate, accommodation.getId());
        // Get the number of people booked for the accommodation between the specified dates
        Integer bookedPeople = bookingHistoryRepo.quantityOfPersonToBeModifiedBetweenGivenDates(startDate, endDate, bookingId, accommodation.getId());

        // Default to 0 if no values are returned
        if (availableCapacity == null) availableCapacity = 0;
        if (bookedPeople == null) bookedPeople = 0;

        // Calculate the updated available capacity after considering the new booking
        availableCapacity -= bookedPeople;
        availableCapacity += personQuantity;

        // Check if the updated capacity is within the maximum allowed capacity
        return availableCapacity <= accommodation.getMaxCapacity();
    }

    /**
     * Updates the booking information in the repository and sends a confirmation email to the user.
     *
     * @param booking The booking to be updated.
     * @param startDate The new start date of the booking.
     * @param endDate The new end date of the booking.
     * @param personQuantity The number of people for the booking.
     * @param accommodation The accommodation for the booking.
     */
    private void updateBooking(BookingHistory booking, LocalDate startDate, LocalDate endDate, Integer personQuantity, Accommodation accommodation) {
        // Calculate the number of days between the start and end dates
        int daysBetween = Math.toIntExact(ChronoUnit.DAYS.between(startDate, endDate));
        // Calculate the total cost of the booking
        int totalCost = (accommodation.getPricePerDayPerPerson() * personQuantity) * daysBetween;

        // Update the booking history and booking repository with the new details
        bookingHistoryRepo.updateBookingHistory(startDate, endDate, totalCost, daysBetween, personQuantity, booking.getId());
        bookingRepo.updateBooking(startDate, endDate, totalCost, daysBetween, personQuantity, booking.getId());

        // Retrieve the currently logged-in user
        User loggedInUser = UserController.getLogginInUser();
        // Send a confirmation email to the user about the booking modification
        emailSend.send(loggedInUser.getEmail(), "A '" + booking.getId() + "' számú azonosítójú foglalásod az alábbiak szerint módosúlt",
                "Szállás neve: " + accommodation.getName() +
                        "\nBeköltözés napja: " + startDate +
                        "\nKiköltözés napja: " + endDate +
                        "\nSzemélyek száma: " + personQuantity +
                        "\nFizetendő összeg: " + totalCost + " Ft" +
                        "\n\nA szállás " + startDate + " napon 12:00-tól költözhető be!\nKérjük, hogy " +
                        endDate + " napon legkésőbb délig adják át a kulcsot a kiköltözés után.");
    }
}
