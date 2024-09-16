package com.szallasd.szallasok.controller;

import com.szallasd.szallasok.entity.Booking;
import com.szallasd.szallasok.entity.BookingHistory;
import com.szallasd.szallasok.entity.User;
import com.szallasd.szallasok.entity.Accommodation;
import com.szallasd.szallasok.repository.*;
import com.szallasd.szallasok.service.UserBookings;
import com.szallasd.szallasok.service.EmailSend;
import com.szallasd.szallasok.service.BookingModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

// the implementation of all functions is organized into classes
// where I have written a detailed explanation for each
@Controller
public class BookingController {
    public static List<BookingHistory> bookingHistory = new ArrayList<>();

    // Public fields to track user roles and login status
    public boolean userIsAdmin = false;
    public boolean userIsRental = false;
    public boolean userIsBooking = false;
    public boolean nobodyIsLoggedIn = true;

    // Repositories for various entities
    EvaluationsRepo evaluationsRepo;
    UserRepo userRepo;
    BookingHistoryRepo bookingHistoryRepo;
    BookingRepo bookingRepo;
    AccommodationRepo accommodationRepo;

    // Reference to the currently logged-in user
    User loggedUser;

    // Entities
    BookingHistory bookingToModify;
    Accommodation selectedAccommodation;

    // The exact date when the user is submit the booking
    LocalDate bookingDate;
    @Autowired
    private final EmailSend emailSend;

    // Constructor for dependency injection of repositories
    public BookingController(EvaluationsRepo evaluationsRepo, UserRepo userRepo, BookingHistoryRepo bookingHistoryRepo, BookingRepo bookingRepo, AccommodationRepo accommodationRepo, EmailSend emailSend) {
        this.evaluationsRepo = evaluationsRepo;
        this.userRepo = userRepo;
        this.bookingHistoryRepo = bookingHistoryRepo;
        this.bookingRepo = bookingRepo;
        this.accommodationRepo = accommodationRepo;
        this.emailSend = emailSend;
    }

    public static List<BookingHistory> getBookingHistory() {
        return bookingHistory;
    }

    @GetMapping(value = "/foglalasaim")
    public String bookingsHandler(Model model,
                                  RedirectAttributes redirectAttributes) {
        // Retrieve user status for menu display
        nobodyIsLoggedIn = UserController.getNobodyIsLoggedIn();
        userIsAdmin = UserController.getAdminIsLoggedIn();
        userIsRental = UserController.getRentalIsLoggedIn();
        userIsBooking = UserController.getBookingIsLoggedIn();

        // If no user is logged in, redirect to login/registration page
        if (nobodyIsLoggedIn) {
            return "redirect:/log_reg";
        }

        // Add user status attributes to the model for menu display
        model.addAttribute("berlo", userIsBooking);
        model.addAttribute("ures", nobodyIsLoggedIn);
        model.addAttribute("kiado", userIsRental);
        model.addAttribute("admin", userIsAdmin);

        // Create an instance of UserBookings to manage userBookingsManager
        UserBookings userBookingsManager = new UserBookings(bookingHistoryRepo);
        loggedUser = UserController.getLogginInUser();

        // If no user is logged in, redirect to login/registration page
        if (loggedUser == null) {
            return "/log_reg";
        }
        // If the user is a renter, show their userBookingsManager
        if (loggedUser.getUserType().equals("Bérlő")) {
            // Retrieve the list of userBookingsManager for the logged-in user
            bookingHistory = userBookingsManager.listOfTheUserBookings(bookingHistory, loggedUser);

            // Check if the user has any bookings, past or present
            if (bookingHistory.isEmpty()) {
                redirectAttributes.addFlashAttribute("Uzenet", "Még nincs egy lefoglalt szállásod sem!");
                return "redirect:/";
            }

            // If the user has bookings, add them to the model and display them
            model.addAttribute("elozmenyek", bookingHistory);
            return "user_bookings";
        } else {
            // Redirect non-renter users to the accommodations page
            return "redirect:/szallasok";
        }
    }

    // Modifying an existing booking
    @GetMapping(value = "foglalas_modositas")
    public String booking_modification(Model model,
                                       @RequestParam(value = "foglalasAzonosito", required = false) Integer bookingIdToModify,
                                       @RequestParam(value = "kezdo", required = false) String startingDate,
                                       RedirectAttributes redirectAttributes) {
        // Check if a user is logged in
        if (loggedUser == null) {
            return "/log_reg";
        }
        // if the logged-in user is renter
        if (loggedUser.getUserType().equals("Bérlő")) {
            // If a starting date is provided, parse it
            if (startingDate != null) {
                bookingDate = LocalDate.parse(startingDate);
            }
            // Get the current date
            LocalDate currentDate = LocalDate.now();

            // Ensure the booking start date is in the future
            if (currentDate.isAfter(bookingDate) || currentDate.isEqual(bookingDate)) {
                redirectAttributes.addFlashAttribute("Uzenet", "Csak a beköltözés előtti napig módosíthatod vagy törölheted foglalásodat!");
                return "redirect:/foglalasaim";
            }

            if (bookingIdToModify != null) {
                // Find the booking to modify
                bookingToModify = bookingHistoryRepo.findById(bookingIdToModify).orElse(null);
                boolean isBookingAlreadyCancelled = true;

                // Check if the booking has already been cancelled
                for (Booking booking : bookingRepo.listOfTheBookings()) {
                    if (Objects.equals(booking.getId(), bookingToModify.getId())) {
                        isBookingAlreadyCancelled = false;
                        break;
                    }
                }
                // If the booking has been cancelled, prevent modification or deletion
                if (isBookingAlreadyCancelled) {
                    redirectAttributes.addFlashAttribute("Uzenet", "Ez a foglalás sajnos már törlésre került a kiadó által szóval sem módosítani, sem törölni nem tudod már!");
                    return "redirect:/foglalasaim";
                }

                // Retrieve the accommodation details
                selectedAccommodation = accommodationRepo.findById(bookingToModify.getAccommodationId()).orElse(null);


                // Calculate available capacity
                Integer availableCapacity = bookingHistoryRepo.getAvailableCapacityBetweenDates(
                        bookingToModify.getBookingStartDate(), bookingToModify.getBookingEndDate(), bookingToModify.getAccommodationId());

                // If availableCapacity is null, set it to zero
                availableCapacity = availableCapacity == null ? 0 : selectedAccommodation.getMaxCapacity() - availableCapacity;

                // Add attributes to the model for the view
                model.addAttribute("szallas", bookingToModify);
                model.addAttribute("szallasKapacitas", availableCapacity);
                return "booking_modification";
            } else {
                // Redirect if no booking ID is provided
                return "redirect:/foglalasaim";
            }
        } else {
            // Redirect non-renter users to the accommodations page
            return "redirect:/szallasok";
        }
    }

    @GetMapping(value = "foglalas_modosit")
    public String booking_modification_process(RedirectAttributes redirectAttributes,
                                               @RequestParam(value = "date_start", required = false) String startDate,
                                               @RequestParam(value = "date_end", required = false) String endDate,
                                               @RequestParam(value = "quantity", required = false) Integer quantity) {
        // Check if a user is logged in
        if (loggedUser == null) {
            return "/log_reg";
        }
        // if the logged-in user is a renter
        if (loggedUser.getUserType().equals("Bérlő")) {
            boolean isBookingAlreadyCancelled = true;

            // Check if the booking has already been cancelled by the host
            for (Booking booking : bookingRepo.listOfTheBookings()) {
                if (Objects.equals(booking.getId(), bookingToModify.getId())) {
                    isBookingAlreadyCancelled = false;
                    break;
                }
            }

            // If booking has been cancelled, handle the case and show an error message
            if (isBookingAlreadyCancelled) {
                redirectAttributes.addFlashAttribute("Uzenet", "Ez a foglalás sajnos már törlésre került a kiadó által szóval sem módosítani, sem törölni nem tudod már!");
                return "redirect:/foglalasaim";
            }

            // Validate the provided dates
            if (Objects.equals(startDate, "") || Objects.equals(endDate, "")) {
                redirectAttributes.addFlashAttribute("Uzenet", "Érvénytelen dátumot adtál meg!");
                return "redirect:/foglalas_modositas";
            }

            // Initialize the booking modification process
            BookingModification bookingModification = new BookingModification(emailSend, bookingHistoryRepo, accommodationRepo, bookingRepo);
            UserBookings userBookings = new UserBookings(bookingHistoryRepo);

            LocalDate start_date = LocalDate.parse(startDate);
            LocalDate end_date = LocalDate.parse(endDate);

            // Set default quantity if not provided
            if (quantity == null) {
                quantity = 1;
            }

            bookingToModify.setLoggedInUser(loggedUser);
            String message = bookingModification.modifyBooking(bookingToModify, start_date, end_date, quantity);

            // Refresh the list of bookings for the user
            bookingToModify = bookingHistoryRepo.findById(bookingToModify.getId()).orElse(null);
            bookingHistory.clear();
            bookingHistory = userBookings.listOfTheUserBookings(bookingHistory, loggedUser);

            // Add success message and redirect to the booking modification page
            redirectAttributes.addFlashAttribute("Uzenet", message);
            return "redirect:/foglalas_modositas";
        } else {
            // Redirect non-renters to the accommodations page
            return "redirect:/szallasok";
        }
    }

    @GetMapping(value = "foglalas_torlese")
    public String booking_delete(@RequestParam("torlendoAzonosito") Integer bookingIdToCancel, @RequestParam("kezdo") String startDate, RedirectAttributes redirectAttributes) {
        boolean isBookingAlreadyCancelled = true;

        // Check if the booking has already been cancelled by the renter
        for (Booking booking : bookingRepo.listOfTheBookings()) {
            if (Objects.equals(booking.getId(), bookingIdToCancel)) {
                isBookingAlreadyCancelled = false;
                break;
            }
        }

        // booking has been cancelled
        if (isBookingAlreadyCancelled) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ez a foglalás sajnos már törlésre került a kiadó által szóval sem módosítani, sem törölni nem tudod már!");
            return "redirect:/foglalasaim";
        }

        LocalDate start_date = LocalDate.parse(startDate);
        LocalDate datum = LocalDate.now();

        // if the current date is after or equal then the starting date
        if (datum.isAfter(start_date) || datum.isEqual(start_date)) {
            redirectAttributes.addFlashAttribute("Uzenet", "Csak a beköltözés előtti napig módosíthatod vagy törölheted foglalásodat!");
            return "redirect:/foglalasaim";
        }

        // Retrieve the booking and send a cancellation email to both the host and the renter
        BookingHistory bookingToBeDeleted = bookingHistoryRepo.findById(bookingIdToCancel).orElse(null);

        // Get the current date
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        String currentDate = dateFormat.format(date);
        assert bookingToBeDeleted != null;

        // Send emails
        emailSend.send(loggedUser.getEmail(), "A '" + bookingToBeDeleted.getId() + "' számú azonosítójú foglalásod " + currentDate + "-kor lemondásra került!\n\n\n\nSzállás neve: " + bookingToBeDeleted.getAccommodationName() + "\n\nLemondott szállás időintervalluma:" + bookingToBeDeleted.getBookingStartDate() + "-tól " + bookingToBeDeleted.getBookingEndDate() + "-ig\n\nSajnáljuk, hogy nem jött össze a mostani foglalásod, de reméljük, hogy hamarosan újra nálunk foglalsz.\n\nÜdvüzlettel: A Szállás Neked csapata", "Foglalás lemondása");
        bookingHistoryRepo.deleteById(bookingIdToCancel);

        // Delete booking
        bookingRepo.deleteById(bookingIdToCancel);
        bookingHistory.clear();
        emailSend.send(bookingToBeDeleted.getRentalUserEmail(), "Sajnálattal értesítjük, hogy a " + bookingToBeDeleted.getAccommodationName() + "hoz tartozó " + bookingToBeDeleted.getBookingStartDate() + " - " + bookingToBeDeleted.getBookingEndDate() + " közötti foglalást lemondták!\n\nFoglaló email címe: " + loggedUser.getEmail() + "\nSzemélyek száma: " + bookingToBeDeleted.getPersonQuantity() + "\n\nÜdvüzlettel: A Szállás Neked csapata", "Foglalás lemondása");
        redirectAttributes.addFlashAttribute("Uzenet", "Sikeresen lemondtad a foglalásod!");
        return "redirect:/foglalasaim";
    }
}
