package com.szallasd.szallasok.controller;

import com.szallasd.szallasok.entity.Accommodation;
import com.szallasd.szallasok.entity.BookingHistory;
import com.szallasd.szallasok.entity.User;
import com.szallasd.szallasok.entity.Booking;
import com.szallasd.szallasok.repository.*;
import com.szallasd.szallasok.service.Login;
import com.szallasd.szallasok.service.EmailSend;
import com.szallasd.szallasok.service.GoogleMapService;
import com.szallasd.szallasok.service.Create;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

// the implementation of all functions is organized into classes
// where I have written a detailed explanation for each
@Controller
public class UserController {
    // Static fields to track user roles and login status
    public static boolean userIsAdmin = false;
    public static boolean userIsHost = false;
    public static boolean userIsRenter = false;
    public static boolean nobodyIsLoggedIn = true;
    static User loggedInUser; // Reference to the currently logged-in user

    // Repositories for various entities
    EvaluationsRepo evaluationsRepo;
    UserRepo userRepo;
    BookingHistoryRepo bookingHistoryRepo;
    BookingRepo bookingRepo;
    AccommodationRepo accommodationRepo;

    // State variables related to accommodation and image handling
    boolean accommodationHasImage = true;
    String previousImage = "";

    // Service classes for Google Maps and Email sending
    @Autowired
    private GoogleMapService googleMapService;
    @Autowired
    private EmailSend emailSend;

    // Filters and sorting fields
    private String cityFilter = "";
    private String sorting = "";  // Values: "increasing" or "decreasing"

    // Constructor for dependency injection of repositories
    public UserController(EvaluationsRepo evaluationsRepo, UserRepo userRepo, BookingHistoryRepo bookingHistoryRepo, BookingRepo bookingRepo, AccommodationRepo accommodationRepo) {
        this.evaluationsRepo = evaluationsRepo;
        this.userRepo = userRepo;
        this.bookingHistoryRepo = bookingHistoryRepo;
        this.bookingRepo = bookingRepo;
        this.accommodationRepo = accommodationRepo;
    }

    // Getters for checking the login and role status of the current user
    public static boolean getNobodyIsLoggedIn() {
        return nobodyIsLoggedIn;
    }

    public static boolean getAdminIsLoggedIn() {
        return userIsAdmin;
    }

    public static boolean getBookingIsLoggedIn() {
        return userIsRenter;
    }

    public static boolean getRentalIsLoggedIn() {
        return userIsHost;
    }

    public static User getLogginInUser() {
        return loggedInUser;
    }


    /**
     * Handles requests for login and registration pages.
     * Routes to the login and registration view.
     */
    @RequestMapping(value = {"/log_reg", "/login", "/reg", "/registration"})
    public String logRegHandler() {
        return "log_reg";
    }

    /**
     * Handles user registration process.
     */
    @RequestMapping(value = "/regisztracio_process")
    public String handleRegistration(
            @RequestParam("email") String email,
            @RequestParam("nev") String name,
            @RequestParam("lakcim") String address,
            @RequestParam("telefonszam") String phoneNumber,
            @RequestParam("jelszo") String password,
            @RequestParam("jelszo2") String confirmPassword,
            @RequestParam(name = "jogosultsag", required = false, defaultValue = "") String userType,
            RedirectAttributes redirectAttributes) {

        // Validate user type selection
        if (userType.isEmpty()) {
            redirectAttributes.addFlashAttribute("Uzenet", "Jelöld be, hogy bérlő vagy kiadó szeretnél lenni!");
            return "redirect:/log_reg";
        }

        String registrationStatus = "";
        Create userCreator = new Create(userRepo, bookingRepo, accommodationRepo);

        // Validate password match
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("Uzenet", "Nem egyeznek a megadott jelszavak!");
            return "redirect:/log_reg";
        }

        // Create a new user based on the selected role (renter or host)
        if (userType.equals("Bérlő")) {
            User user = new User(email, userType, name, address, phoneNumber, password);
            registrationStatus = userCreator.createUser(user);
        }
        if (userType.equals("Kiadó")) {
            User user = new User(email, userType, name, address, phoneNumber, password);
            registrationStatus = userCreator.createUser(user);
        }

        // Handle the outcome of registration
        if (registrationStatus.equals("Sikeres mentés!")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Sikeres létrehozás! Most jelentkezz be " + name + "!");
            emailSend.send(email, "Köszönjük, hogy beregisztráltál hozzánk!\n\nÜdvözlettel: A Szállás Neked csapata", "Köszöntünk az oldalunkon " + name + "!");
        } else {
            redirectAttributes.addFlashAttribute("Uzenet", registrationStatus);
        }

        return "redirect:/log_reg";
    }

    /**
     * Handles the user login process.
     */
    @RequestMapping(value = "/bejelentkezes")
    public String loginUser(
            @RequestParam("email") String email,
            @RequestParam("psw") String password,
            RedirectAttributes redirectAttributes) {

        // Use the Login service to authenticate the user
        Login loginService = new Login(userRepo);
        String loginMessage = loginService.userLogin(email, password);

        if (loginMessage.isEmpty()) {
            redirectAttributes.addFlashAttribute("Uzenet", "Nincs ilyen emaillel regisztált felhasználó!");
            return "redirect:/log_reg";
        }

        if (loginMessage.equals("Van email")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Rossz jelszó!");
            return "redirect:/log_reg";
        }

        if (loginMessage.equals("Sikeres belépés")) {
            // Successfully logged in, set user session details
            loggedInUser = loginService.findUserByEmail(email);
            redirectAttributes.addFlashAttribute("Uzenet", "Sikeres belépés! Üdv újra itt, " + loggedInUser.getName());

            // Determine the role of the user
            if (loggedInUser.getUserType().equals("Admin")) {
                userIsAdmin = true;
            }
            if (loggedInUser.getUserType().equals("Kiadó")) {
                userIsHost = true;
            }
            if (loggedInUser.getUserType().equals("Bérlő")) {
                userIsRenter = true;
            }

            nobodyIsLoggedIn = false;
            return "redirect:/";
        }

        return "log_reg";
    }

    /**
     * Handles the password reminder process by sending an email with the current password.
     */
    @RequestMapping(value = "/jelszo_emlekezteto")
    public String sendPasswordReminder(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        // Use the Login service to find the user by email
        Login loginService = new Login(userRepo);
        User user = loginService.findUserByEmail(email);

        if (user.getName().isEmpty()) {
            redirectAttributes.addFlashAttribute("Uzenet", "Nincs regisztráció ezzel az email címmel: " + email + "!");
            return "redirect:/log_reg";
        } else {
            redirectAttributes.addFlashAttribute("Uzenet", "A jelszó emlékeztetőt elküldtük a megadott email címedre, " + user.getName() + "!");
            emailSend.send(email, user.getName() + ", a jelszavad a következő: " + user.getPassword() + "\n\nÜdvözlettel: A Szállás Neked csapata", "Jelszó emlékeztető");
        }

        return "redirect:/log_reg";
    }

    /**
     * Handles displaying the list of all users, excluding the admin.
     */
    @RequestMapping(value = "/users")
    public String showAllUsers(Model model) {
        if (nobodyIsLoggedIn) {
            return "log_reg";
        }

        // Collect all users except the admin for display
        List<User> users = new ArrayList<>();
        for (User user : userRepo.listOfTheUsers()) {
            if (!user.getEmail().equals("admin")) {
                users.add(user);
            }
        }

        model.addAttribute("users", users);
        return "users";
    }

    /**
     * Handles the deletion of a user account.
     * Sends a notification email with the reason for deletion.
     */
    @PostMapping(value = "/users/{email}")
    public String deleteUser(
            @PathVariable("email") String email,
            @RequestParam(name = "reason", required = false) String reason) {

        // Find and delete the user based on email
        for (User user : userRepo.listOfTheUsers()) {
            if (user.getEmail().equals(email)) {
                userRepo.delete(user);
                emailSend.send(user.getEmail(), "Kedves " + user.getName() + "!\n\nFiókodat töröltük a következő okok miatt:\n" + reason + "\n\nÜdvözlettel: A Szállás Neked csapata", "Felhasználó törölve");
                break;
            }
        }

        return "redirect:/users";
    }

    /*
     * User profile modification
     */
    @GetMapping(value = "/profil")
    public String profilEditHandler(Model model) {
        if (nobodyIsLoggedIn) {
            return "log_reg";
        }
        // Add necessary attributes for menu rendering
        model.addAttribute("berlo", userIsRenter);
        model.addAttribute("ures", nobodyIsLoggedIn);
        model.addAttribute("kiado", userIsHost);
        model.addAttribute("admin", userIsAdmin);

        // If no one is logged in, redirect to login/register page
        if (nobodyIsLoggedIn) {
            return "log_reg";
        }

        // Add logged-in user's details to the model
        model.addAttribute("felhasznalo", loggedInUser);
        return "profile";
    }

    @PostMapping(value = "/profil_szerkeszt")
    public String editProfile(
            @RequestParam(value = "name", defaultValue = "") String newName,
            @RequestParam(value = "psw", defaultValue = "") String password,
            @RequestParam(value = "psw2", defaultValue = "") String passwordConfirm,
            @RequestParam(value = "tel", defaultValue = "") String phoneNumber,
            @RequestParam(value = "cim", defaultValue = "") String address,
            RedirectAttributes redirectAttributes
    ) {
        // Default values based on currently logged-in user's data
        String updatedName = loggedInUser.getName();
        String updatedAddress = loggedInUser.getAddress();
        String updatedPhone = loggedInUser.getPhone();
        String updatedPassword = loggedInUser.getPassword();

        // Check for empty name field
        if (newName.isEmpty()) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a nevedet muszáj egy új nevet beírnod!");
            return "redirect:/profil";
        }
        // Check for empty address field
        if (address.isEmpty()) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a címedet muszáj egy új címet beírnod!");
            return "redirect:/profil";
        }
        // Check for empty phone field
        if (phoneNumber.isEmpty()) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a telefonszámodat muszáj egy új számot beírnod!");
            return "redirect:/profil";
        }
        // Password validation if a new one is provided
        if (!password.isEmpty()) {
            if (!password.equals(passwordConfirm)) {
                redirectAttributes.addFlashAttribute("Uzenet", "Nem egyeznek a megadott jelszavak!");
                return "redirect:/profil";
            } else {
                updatedPassword = password;
            }
        }
        // Check if no changes have been made
        if (newName.equals(updatedName) && phoneNumber.equals(updatedPhone) && address.equals(updatedAddress) && updatedPassword.equals(loggedInUser.getPassword())) {
            redirectAttributes.addFlashAttribute("Uzenet", "Nem történt módosítás");
            return "redirect:/profil";
        }
        // Update fields only if there are changes
        if (!updatedName.equals(newName)) {
            updatedName = newName;
        }
        if (!updatedAddress.equals(address)) {
            updatedAddress = address;
        }
        if (!updatedPhone.equals(phoneNumber)) {
            updatedPhone = phoneNumber;
        }

        // Persist the changes in the database
        userRepo.updateUser(updatedName, updatedPhone, updatedAddress, updatedPassword, loggedInUser.getEmail());

        // Update session with new user data
        loggedInUser.setName(updatedName);
        loggedInUser.setPhone(updatedPhone);
        loggedInUser.setAddress(updatedAddress);
        loggedInUser.setPassword(updatedPassword);

        redirectAttributes.addFlashAttribute("Uzenet", "Sikeres módosítás!");
        return "redirect:/profil";
    }

    /*
     * User logout
     */
    @RequestMapping(value = "/kilepes")
    public String logout(RedirectAttributes redirectAttributes) {
        userIsRenter = false;
        userIsAdmin = false;
        userIsHost = false;
        nobodyIsLoggedIn = true;

        // Clear booking history when user logs out
        BookingController.getBookingHistory().clear();

        redirectAttributes.addFlashAttribute("Uzenet", "Logout successful!");
        return "redirect:/log_reg";
    }

    // ------------------------------------------ Accommodation management ---------------------------------------------------
    @GetMapping(value = {"/", "/szallasok"})
    public String welcomeHandler(Model model) {
        // Add necessary attributes for menu rendering
        model.addAttribute("berlo", userIsRenter);
        model.addAttribute("ures", nobodyIsLoggedIn);
        model.addAttribute("kiado", userIsHost);
        model.addAttribute("admin", userIsAdmin);

        // Fetch the list of accommodations
        List<Accommodation> accommodationList = accommodationRepo.listOfTheAccommodation();

        // Sort accommodations by price per person per day, either ascending or descending
        if (Objects.equals(sorting, "") || Objects.equals(sorting, "ascending")) {
            accommodationList.sort(Comparator.comparing(Accommodation::getPricePerDayPerPerson));
        } else {
            accommodationList.sort(Comparator.comparing(Accommodation::getPricePerDayPerPerson).reversed());
        }

        // Filter accommodations by city if a filter is provided
        if (!Objects.equals(cityFilter, "")) {
            List<Accommodation> filteredAccommodations = new ArrayList<>();
            for (Accommodation accommodation : accommodationList) {
                if (accommodation.getLocation().toLowerCase().contains(cityFilter.toLowerCase())) {
                    filteredAccommodations.add(accommodation);
                }
            }
            model.addAttribute("accommodations", filteredAccommodations);
        } else {
            model.addAttribute("accommodations", accommodationList);
        }

        // Add recommended accommodations
        model.addAttribute("recommendedAccommodations", accommodationRepo.listOfRecommendations());

        // Reset filters
        cityFilter = "";
        sorting = "";
        return "accommodations";
    }

    @PostMapping(value = {"/", "/szallasok"})
    public String accommodationListGet(
            @RequestParam("telepules") String city,
            @RequestParam("rendezes") String sortOrder
    ) {
        cityFilter = city;
        sorting = sortOrder;
        return "redirect:/szallasok";
    }

    @GetMapping(value = "/szallas/{id}")
    public String accommodationBack(
            @PathVariable("id") int id,
            Model model
    ) {
        // Add necessary attributes for menu rendering
        model.addAttribute("isRenter", userIsRenter);
        model.addAttribute("isLoggedOut", nobodyIsLoggedIn);
        model.addAttribute("isHost", userIsHost);
        model.addAttribute("isAdmin", userIsAdmin);

        // Fetch accommodation details by ID
        Accommodation accommodation = accommodationRepo.findById(id).orElse(null);
        model.addAttribute("accommodation", accommodation);

        // Generate Google Maps link for accommodation location
        assert accommodation != null;
        String address = accommodation.getLocation();
        System.out.println(address);
        model.addAttribute("googleLink", googleMapService.getGoogleMapLink(address));

        return "accommodation";
    }

    @GetMapping(value = "/szallas/{id}/vissza")
    public String accommodationBack(@PathVariable("id") int id) {
        return "redirect:/szallas/{id}";
    }

    /*
     * Booking process
     */
    @GetMapping(value = "/szallas/{id}/foglal")
    public String bookAccommodation(
            @PathVariable("id") int id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // If no user is logged in, redirect to login page
        if (nobodyIsLoggedIn) {
            redirectAttributes.addFlashAttribute("Uzenet", "Előbb kell jelentkezned, hogy foglalhass szállásokat!");
            return "redirect:/log_reg";
        }

        // Add necessary attributes for menu rendering
        model.addAttribute("berlo", userIsRenter);
        model.addAttribute("ures", nobodyIsLoggedIn);
        model.addAttribute("kiado", userIsHost);
        model.addAttribute("admin", userIsAdmin);

        // Fetch accommodation details and pass to the model
        for (Accommodation sz : accommodationRepo.listOfTheAccommodation())
            if (sz.getId() == id)
                model.addAttribute("szallas", sz);
        return "booking";
    }

    /*
     * Booking confirmation and validation
     */
    @PostMapping(value = "/szallas/{id}/foglal")
    public String submitBooking(
            @PathVariable("id") int accommodationId,
            @RequestParam("date_start") String startDate,
            @RequestParam("date_end") String endDate,
            @RequestParam("dbszam") int personCount,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        // Add necessary attributes for menu rendering
        model.addAttribute("berlo", userIsRenter);
        model.addAttribute("ures", nobodyIsLoggedIn);
        model.addAttribute("kiado", userIsHost);
        model.addAttribute("admin", userIsAdmin);

        loggedInUser = UserController.getLogginInUser();
        LocalDate currentDate = LocalDate.now();
        LocalDate parsedStartDate = LocalDate.parse(startDate);
        LocalDate parsedEndDate = LocalDate.parse(endDate);

        // Validate booking dates and ensure the minimum one-night booking requirement is met
        if (startDate.equals(endDate)) {
            redirectAttributes.addFlashAttribute("Uzenet", "Nem adhatod meg ugyanazt a dátumot beköltözés és kiköltözés napjának, minimum 1 napra tudsz csak szállást bérelni!");
            return "redirect:/szallas/{id}/foglal";
        }

        // Ensure both start and end dates are provided
        if (startDate.isEmpty() || endDate.isEmpty()) {
            redirectAttributes.addFlashAttribute("Uzenet", "A dátumok megadása kötelező!");
            return "redirect:/szallas/{id}/foglal";
        }

        // Ensure the booking start date is not in the past or toda
        if (currentDate.isAfter(parsedStartDate) || currentDate.isEqual(parsedStartDate)) {
            redirectAttributes.addFlashAttribute("Uzenet", "A foglalás kezdete nem lehet korábbi vagy a jelenlegi dátummal megegyező időpont!");
            return "redirect:/szallas/{id}/foglal";
        }


        // Check if the requested number of persons exceeds the accommodation's maximum capacity
        Accommodation accommodation = accommodationRepo.findById(accommodationId).orElse(null);
        assert accommodation != null;
        if (personCount > accommodation.getMaxCapacity()) {
            redirectAttributes.addFlashAttribute("Uzenet", "A maximális férőhely: " + Objects.requireNonNull(accommodationRepo.findById(accommodationId).orElse(null)).getMaxCapacity() + "!");
            return "redirect:/szallas/{id}/foglal";
        }


        // Ensure the start date is before the end date
        if (parsedStartDate.isAfter(parsedEndDate)) {
            redirectAttributes.addFlashAttribute("Uzenet", "Nem lehet korábbi időpontra megadni a foglalás kezdetét, mint a végét!");
            return "redirect:/szallas/{id}/foglal";
        }

        // Calculate the number of days between the start and end dates
        int numberOfNights = Math.toIntExact(ChronoUnit.DAYS.between(parsedStartDate, parsedEndDate));

        // Calculate the total booking amount based on the number of people and nights
        int totalAmount = accommodation.getPricePerDayPerPerson() * personCount * numberOfNights;

        // Create the booking object
        Booking booking = new Booking(accommodation, loggedInUser, currentDate, parsedStartDate, parsedEndDate, totalAmount, personCount, numberOfNights);

        // Check the availability of the accommodation during the requested dates
        String accommodationName = accommodation.getName();
        Integer availableCapacity = bookingHistoryRepo.getAvailableCapacityBetweenDates(parsedStartDate, parsedEndDate, accommodationId);

        // If no capacity is found, assume 0 available
        if (availableCapacity == null) {
            availableCapacity = 0;
        }

        // Adjust available capacity by removing existing bookings for the same period
        Integer existingBookings = bookingHistoryRepo.quantityOfPersonToBeModifiedBetweenGivenDates(parsedStartDate, parsedEndDate, booking.getId(), accommodationId);
        if (existingBookings == null) {
            existingBookings = 0;
        }
        availableCapacity -= existingBookings;
        Integer currentlyBooked = availableCapacity;
        int maxPeopleAllowed = accommodation.getMaxCapacity() - availableCapacity;
        availableCapacity += personCount;

        // If capacity allows, save the booking and booking history
        if (availableCapacity <= accommodation.getMaxCapacity()) {
            BookingHistory bookingHistory = new BookingHistory(getLogginInUser(), currentDate, parsedStartDate, parsedEndDate, totalAmount, personCount, numberOfNights, accommodationName, accommodationId, "", accommodation.getRentalUser().getEmail());
            bookingRepo.save(booking);
            bookingRepo.listOfTheBookings().add(booking);
            bookingHistoryRepo.save(bookingHistory);
            bookingHistoryRepo.updateAccommodationImage(accommodation.getImage(), bookingHistory.getId());
            bookingHistoryRepo.listOfBookingHistory().add(bookingHistory);

            // Send email confirmation to both renter and host
            emailSend.send(loggedInUser.getEmail(), "Sikeresen elmentettük a foglalásod a " + accommodation.getName() + "-hoz\n\nÖsszegzés:\n-Foglalás kezdete: " + booking.getStartingDate() + "\n-A szállást az alábbi napon el kell hagyni: " + parsedEndDate + "\n -Személyek száma: " + booking.getPersonQuantity() + "\n-Fizetendő összeg: " + totalAmount + "\n Kiadó: " + accommodation.getRentalUser().getName() + "\n\nSzállás Neked csapata", "A foglalás sikeres!");
            emailSend.send(accommodation.getRentalUser().getEmail(), "Foglalás történt a " + accommodation.getName() + "-ra\n\nFoglalás kezdete: " + startDate + " , Vége: " + endDate + "\nSzemélyek száma: " + personCount + "\n\nBérlő adatai:\n Neve: " + loggedInUser.getName() + "\nE-mail címe: " + loggedInUser.getEmail() + "\nTelefonszáma: " + loggedInUser.getPhone() + "\n\nSzállás Neked csapata", "Foglalás értesítés");
        } else {
            // Handle the scenario where no availability is found for the requested dates

            boolean foundAvailableFutureDate = false;
            LocalDate futureStartDate = parsedStartDate;
            LocalDate futureEndDate = parsedEndDate;

            // Search forward for the next available dates
            while (!foundAvailableFutureDate) {
                futureStartDate = futureStartDate.plusDays(1);
                futureEndDate = futureEndDate.plusDays(1);
                availableCapacity = bookingHistoryRepo.getAvailableCapacityBetweenDates(futureStartDate, futureEndDate, accommodation.getId());
                if (availableCapacity == null) {
                    availableCapacity = 0;
                }
                existingBookings = bookingHistoryRepo.quantityOfPersonToBeModifiedBetweenGivenDates(futureStartDate, futureEndDate, booking.getId(), accommodation.getId());
                if (existingBookings == null) {
                    existingBookings = 0;
                }
                availableCapacity -= existingBookings;
                availableCapacity += personCount;
                if (availableCapacity <= accommodation.getMaxCapacity()) {
                    foundAvailableFutureDate = true;
                }
            }

            boolean foundAvailablePastDate = false;
            boolean canSearchBackward = true;
            LocalDate pastStartDate = parsedStartDate;
            LocalDate pastEndDate = parsedEndDate;

            // Search backward for the next available dates
            while (!foundAvailablePastDate) {
                pastStartDate = pastStartDate.minusDays(1);
                pastEndDate = pastEndDate.minusDays(1);
                if (pastStartDate.isEqual(currentDate)) {
                    canSearchBackward = false;
                    break;
                }
                availableCapacity = bookingHistoryRepo.getAvailableCapacityBetweenDates(pastStartDate, pastEndDate, accommodation.getId());
                if (availableCapacity == null) {
                    availableCapacity = 0;
                }
                existingBookings = bookingHistoryRepo.quantityOfPersonToBeModifiedBetweenGivenDates(pastStartDate, pastEndDate, booking.getId(), accommodation.getId());
                if (existingBookings == null) {
                    existingBookings = 0;
                }
                availableCapacity -= existingBookings;
                availableCapacity += personCount;
                if (availableCapacity <= accommodation.getMaxCapacity()) {
                    foundAvailablePastDate = true;
                }
            }

            // Handle booking failure due to full accommodation for the requested dates
            if (maxPeopleAllowed <= 0) {
                if (canSearchBackward) {
                    redirectAttributes.addFlashAttribute("Uzenet", "Sajnos erre az időszakra már minden helyet lefoglaltak a szálláson! A Legközelebbi szabad időpont a kívánt időponthoz képest " + numberOfNights + " napra " + personCount + " főnek " + futureStartDate + "-tól, vagy ha előbb szeretnél akkor már " + pastStartDate + "-tól lehetséges!");

                }
                if (!canSearchBackward) {
                    redirectAttributes.addFlashAttribute("Uzenet", "Sajnos erre az időszakra már minden helyet lefoglaltak a szálláson! A Legközelebbi szabad időpont a kívánt időponthoz képest " + numberOfNights + " napra " + personCount + " főnek " + futureStartDate + "-tól lehetséges, előbb már sajnos nincs lehetőség!");

                }
            } else {
                if (canSearchBackward) {
                    redirectAttributes.addFlashAttribute("Uzenet", "Sajnos erre az időszakra már csak " + maxPeopleAllowed + " személynek foglalható ez a szállás, mert a többi " + currentlyBooked + " helyet már lefoglalták! A Legközelebbi szabad időpont a kívánt időponthoz képest " + numberOfNights + " napra " + personCount + " főnek " + futureStartDate + "-tól, vagy ha előbb szeretnél akkor már " + pastStartDate + "-tól lehetséges!");

                }
                if (!canSearchBackward) {
                    redirectAttributes.addFlashAttribute("Uzenet", "Sajnos erre az időszakra már csak " + maxPeopleAllowed + " személynek foglalható ez a szállás, mert a többi " + currentlyBooked + " helyet már lefoglalták! A Legközelebbi szabad időpont a kívánt időponthoz képest " + numberOfNights + " napra " + personCount + " főnek " + futureStartDate + "-tól lehetséges, előbb már sajnos nincs lehetőség!");

                }
            }
            return "redirect:/szallas/{id}/foglal";
        }

        // Redirect to booking receipt page after a successful booking
        redirectAttributes.addAttribute("foglalas_id", booking.getId());
        return "redirect:/szallas/{id}/foglal/foglalas_nyugta/{foglalas_id}";
    }

    @GetMapping(value = "/szallas/{id}/foglal/foglalas_nyugta/{foglalas_id}")
    public String bookingReceipt(
            @PathVariable("id") int accommodationId,
            @PathVariable("foglalas_id") int bookingId,
            Model model
    ) {
        // If no user is logged in, redirect to login/registration page
        if (loggedInUser == null) {
            return "redirect:/log_reg";
        }

        // Set menu attributes based on the logged-in user's role
        model.addAttribute("berlo", userIsRenter);
        model.addAttribute("ures", nobodyIsLoggedIn);
        model.addAttribute("kiado", userIsHost);
        model.addAttribute("admin", userIsAdmin);

        // Redirect to login page if no one is logged in
        if (nobodyIsLoggedIn) {
            return "redirect:/log_reg";
        }

        // Get details of the logged-in user
        loggedInUser = UserController.getLogginInUser();

        // Retrieve the accommodation and booking history by ID
        Accommodation currentAccommodation = accommodationRepo.findById(accommodationId).orElse(null);
        BookingHistory currentBooking = bookingHistoryRepo.findById(bookingId).orElse(null);

        // Add the accommodation and booking information to the model for the receipt page
        model.addAttribute("szallas_nyugta", currentAccommodation);
        model.addAttribute("foglalas_nyugta", currentBooking);

        // Return the booking receipt pag
        return "booking_recipe";
    }

    /*
     * Admin Functions
     * This section contains administrative functions for managing accommodations.
     */
    @GetMapping(value = "/szallasok_admin")
    public String accommodationsAdminHandler(Model model) {
        // If no one is logged in, redirect to login/registration page
        if (nobodyIsLoggedIn) {
            return "log_reg";
        }

        // Add the list of all accommodations to the model for the admin view
        model.addAttribute("szallasok", accommodationRepo.listOfTheAccommodation());

        // Return the admin accommodations management page
        return "accommodations_admin";
    }

    @PostMapping(value = "/szallasok_admin/{id}")
    public String deleteAccommodation(
            @PathVariable("id") Integer accommodationId,
            @RequestParam(name = "indoklas", required = false) String deletionReason
    ) {
        // If no one is logged in, redirect to login/registration page
        if (nobodyIsLoggedIn) {
            return "log_reg";
        }

        // Find the accommodation in the list and delete it
        for (Accommodation accommodation : accommodationRepo.listOfTheAccommodation()) {
            if (Objects.equals(accommodation.getId(), accommodationId)) {
                accommodationRepo.delete(accommodation);

                // Notify the accommodation's host via email about the deletion
                emailSend.send(
                        accommodation.getRentalUser().getEmail(),
                        "Kedves " + accommodation.getRentalUser().getName() + "!\n\nAz alábbi szállásodat töröltük:\n\nNév: " + accommodation.getName() +
                                "\nHelyszín: " + accommodation.getLocation() + "\nLeírás:\n" + accommodation.getDescription() +
                                "\n\nTörlés indoklása:\n" + deletionReason,
                        "Szállás törölve admin által"
                );
                break;
            }
        }

        // After deletion, redirect back to the admin accommodations management page
        return "redirect:/szallasok_admin";
    }

    @GetMapping(value = "/hirdeteseim")
    public String listingsHandler(Model model) {
        // Redirect to login page if no one is logged in
        if (nobodyIsLoggedIn) {
            return "log_reg";
        }

        // Retrieve all accommodations and filter those belonging to the logged-in user
        List<Accommodation> allAccommodations = accommodationRepo.listOfTheAccommodation();
        List<Accommodation> myListings = new ArrayList<Accommodation>();

        for (Accommodation f : allAccommodations) {
            if (Objects.equals(f.getRentalUser().getEmail(), loggedInUser.getEmail())) {
                myListings.add(f);
            }
        }

        // Add user's listings and whether the accommodation has an image to the model
        model.addAttribute("szallasok", myListings);
        model.addAttribute("vanKep", accommodationHasImage);

        return "rental_ads";
    }

    @GetMapping(value = "/hirdeteseim/{id}/szerkeszt")
    public String editListing(
            @PathVariable("id") int id,
            Model model
    ) {
        // Redirect to login page if no one is logged in
        if (nobodyIsLoggedIn) {
            return "redirect:/log_reg";
        }

        // Retrieve accommodation by ID and add it to the model for editing
        Accommodation accommodation = accommodationRepo.findById(id).orElse(null);
        model.addAttribute("szallas", accommodation);

        return "rental_ads_modification";
    }

    @PostMapping(value = "/hirdeteseim/{id}/szerkesztes")
    public String updateListing(
            @PathVariable("id") int id, @RequestParam(name = "nev", required = false) String name,
            @RequestParam(name = "cim", required = false) String address,
            @RequestParam(name = "ar", required = false) Integer price,
            @RequestParam(name = "ferohely", required = false) Integer capacity,
            @RequestParam(name = "leiras", required = false) String description,
            @RequestParam(name = "img", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes
    ) {
        // Retrieve current accommodation data
        Accommodation accommodation = accommodationRepo.findById(id).orElse(null);
        if (accommodation == null) {
            return "redirect:/my_listings";
        }

        // Store the current values if no new value is provided
        String updatedName = accommodation.getName();
        String updatedAddress = accommodation.getLocation();
        Integer updatedPrice = accommodation.getPricePerDayPerPerson();
        Integer updatedCapacity = accommodation.getMaxCapacity();
        String updatedDescription = accommodation.getDescription();

        // Validate inputs; if any mandatory field is left empty, show an error message
        if (name.isEmpty()) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a szállásod nevét muszáj egy új nevet beírnod!");
            return "redirect:/hirdeteseim/{id}/szerkeszt";
        }
        if (address.isEmpty()) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a szállásod címét muszáj egy új címet beírnod!");
            return "redirect:/hirdeteseim/{id}/szerkeszt";
        }
        if (price == null) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a szállásod árát muszáj egy új árat beírnod!");
            return "redirect:/hirdeteseim/{id}/szerkeszt";
        }
        if (capacity == null) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a szállásod kapacitását muszáj egy új férőhely számot beírnod!");
            return "redirect:/hirdeteseim/{id}/szerkeszt";
        }
        if (description.isEmpty()) {
            redirectAttributes.addFlashAttribute("Uzenet", "Ha módosítani szeretnéd a szállásod leírását muszáj valamit beírnod!");
            return "redirect:/hirdeteseim/{id}/szerkeszt";
        }

        // Update fields if the new values are different from the existing ones
        if (!updatedName.equals(name)) {
            updatedName = name;
        }
        if (!updatedAddress.equals(address)) {
            updatedAddress = address;
        }
        if (!Objects.equals(updatedPrice, price)) {
            updatedPrice = price;
        }
        if (!Objects.equals(updatedCapacity, capacity)) {
            updatedCapacity = capacity;
        }
        if (!updatedDescription.equals(description)) {
            updatedDescription = description;
        }

        // Handle image update: if the accommodation has an image, store it as the previous image
        if (!accommodation.getImage().isEmpty()) {
            previousImage = accommodation.getImage();
        }

        // Update accommodation details in the database
        accommodationRepo.update(updatedPrice, updatedAddress, updatedDescription, updatedCapacity, updatedName, id);

        // Handle image upload if a new image is provided
        String newImage = "";
        if (imageFile != null) {
            String imageName = StringUtils.cleanPath(Objects.requireNonNull(imageFile.getOriginalFilename()));

            // Validate the image file name
            if (imageName.contains("..")) {
                redirectAttributes.addFlashAttribute("Uzenet", "Érvénytelen képformátum!");
                return "redirect:/hirdeteseim/{id}/szerkeszt";
            }

            try {
                // Ensure the image size is under the 0.8MB limit
                float imageSize = imageFile.getSize();
                if (imageSize <= 800000) {
                    newImage = Base64.getEncoder().encodeToString(imageFile.getBytes());

                    // Update image in the database only if a new image is provided or the old one is unchanged
                    if (!newImage.isEmpty()) {
                        accommodationRepo.updateImage(newImage, id);
                    } else if (!previousImage.isEmpty()) {
                        accommodationRepo.updateImage(previousImage, id);
                    }

                    redirectAttributes.addFlashAttribute("Uzenet", "Sikeres módosítás!");
                } else {
                    redirectAttributes.addFlashAttribute("Uzenet", "Túl nagy méretű képet próbálsz meg feltölteni, a max méret 0,8Mb! Az általad feltöltött kép mérete: " + imageSize / 1000000 + "Mb");
                }
                return "redirect:/hirdeteseim/{id}/szerkeszt";
            } catch (IOException e) {
                // Handle unexpected errors during image processing
                redirectAttributes.addFlashAttribute("Uzenet", "Váratlan hiba történt, kérjük próbáld újra!");
                return "redirect:/hirdeteseim/{id}/szerkeszt";
            }
        }
        // Redirect to edit page after processing
        return "redirect:/hirdeteseim/{id}/szerkeszt";
    }


    @PostMapping(value = "/hirdeteseim/{id}/kep_torlese")
    public String deleteImage(@PathVariable("id") int listingId,
                              @RequestParam(name = "img", required = false) MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {
        // Check if an image file is provided
        if (imageFile != null) {
            String imageName = StringUtils.cleanPath(Objects.requireNonNull(imageFile.getOriginalFilename()));

            // Validate the image file name for security
            if (imageName.contains("..")) {
                redirectAttributes.addFlashAttribute("Uzenet", "Érvénytelen képformátum!");
                return "redirect:/hirdeteseim/{id}/szerkeszt";
            }
        }

        // Remove the image from the listing by setting it to an empty string
        previousImage = "";
        accommodationRepo.updateImage(previousImage, listingId);

        redirectAttributes.addFlashAttribute("Uzenet", "Sikeres képeltávolítás");
        return "redirect:/hirdeteseim/{id}/szerkeszt";

    }

    @GetMapping(value = "/kiado_uj_hirdetes")
    public String newListingPage() {
        // Redirect to login page if no one is logged in
        if (nobodyIsLoggedIn) {
            return "redirect:/log_reg";
        }
        return "rental_new_ad";
    }

    @PostMapping(value = "/kiado_uj_hirdetes")
    public String createNewListing(
            @RequestParam("name") String listingName,
            @RequestParam("cim") String listingAddress,
            @RequestParam("ar") String pricePerDay,
            @RequestParam("ferohely") Integer maxCapacity,
            @RequestParam("leiras") String description,
            @RequestParam(name = "img", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes
    ) {
        // Redirect to login page if no one is logged in
        if (nobodyIsLoggedIn) {
            return "redirect:/log_reg";
        }

        // Validate image file size and format
        assert imageFile != null;
        double kepMerete = imageFile.getSize();
        if (kepMerete > 800000) {
            redirectAttributes.addFlashAttribute("Uzenet", "Túl nagy méretű képet próbálsz meg feltölteni, a max méret 0,8Mb! Az általad feltöltött kép mérete: " + kepMerete / 1000000 + "Mb");
            return "redirect:/kiado_uj_hirdetes";
        }


        String imageName = StringUtils.cleanPath(Objects.requireNonNull(imageFile.getOriginalFilename()));
        String encodedImage = "";
        if (imageName.contains("..")) {
            redirectAttributes.addFlashAttribute("Uzenet", "Érvénytelen képformátum!");
            return "redirect:/kiado_uj_hirdetes";
        }

        try {
            // Encode image to Base64
            encodedImage = Base64.getEncoder().encodeToString(imageFile.getBytes());

            // Create a new accommodation listing and save it to the repository
            Accommodation newListing = new Accommodation(
                    listingName, loggedInUser,
                    listingAddress, description,
                    Integer.parseInt(pricePerDay),
                    maxCapacity,
                    ""
            );
            accommodationRepo.save(newListing);
            accommodationRepo.listOfTheAccommodation().add(newListing);

            // Update the new listing with the encoded image
            accommodationRepo.updateImage(encodedImage, newListing.getId());

            redirectAttributes.addFlashAttribute("Uzenet", "Sikeresen létrehoztad az új " + newListing.getName() + " hírdetésedet!");
            return "redirect:/hirdeteseim";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("Uzenet", "Váratlan hiba történt, kérjük próbáld újra!");
            return "redirect:/kiado_uj_hirdetes";
        }
    }

    @GetMapping(value = "szallas_torlese")
    public String deleteListing(@RequestParam("szallasAzonosito") Integer id, RedirectAttributes redirectAttributes) {

        LocalDate today = LocalDate.now();
        Accommodation listingToDelete = accommodationRepo.findById(id).orElse(null);

        if (listingToDelete != null) {
            // Notify users of the cancellation of their bookings for this listing
            for (BookingHistory elozmenyek : bookingHistoryRepo.listOfBookingHistory()) {
                if ((elozmenyek.getAccommodationId().equals(listingToDelete.getId()) && elozmenyek.getBookingStartDate().isEqual(today)) || elozmenyek.getAccommodationId().equals(listingToDelete.getId()) && elozmenyek.getBookingStartDate().isAfter(today)) {
                    emailSend.send(elozmenyek.getLoggedInUser().getEmail(),
                            "Ezúton értesítünk, hogy sajnos a " + listingToDelete.getName() + "hoz tartozó foglalásodat lemondták a kiadó által.\nA kellemetlenségért elnézést kérünk.\n\nÜdvözlettel: A Szállás Neked csapata",
                            "Szállás lemondásra került"
                    );
                }
            }
            // Delete the listing from the repository
            accommodationRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("Uzenet", "Sikeresen törölted a hírdetésedet!");
            return "redirect:/hirdeteseim";
        } else {
            redirectAttributes.addFlashAttribute("Uzenet", "Nem található a hírdetés!");
        }

        return "redirect:/hirdeteseim";
    }
}
