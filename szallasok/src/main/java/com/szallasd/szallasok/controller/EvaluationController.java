package com.szallasd.szallasok.controller;

import com.szallasd.szallasok.entity.Accommodation;
import com.szallasd.szallasok.entity.Evaluations;
import com.szallasd.szallasok.entity.User;
import com.szallasd.szallasok.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// the implementation of all functions is organized into classes
// where I have written a detailed explanation for each
@Controller
public class EvaluationController {
    // Repositories for various entities
    EvaluationsRepo evaluationsRepo;
    UserRepo userRepo;
    BookingHistoryRepo bookingHistoryRepo;
    BookingRepo bookingRepo;
    AccommodationRepo accommodationRepo;

    // Public fields to track user roles and login status
    public boolean userIsAdmin = false;
    public boolean userIsHost = false;
    public boolean userIsRenter = false;
    public boolean nobodyLoggedIn = true;
    User loggedInUser; // Reference to the currently logged-in user

    // Constructor for dependency injection of repositories
    public EvaluationController(EvaluationsRepo evaluationsRepo, UserRepo userRepo, BookingHistoryRepo bookingHistoryRepo, BookingRepo bookingRepo, AccommodationRepo accommodationRepo) {
        this.evaluationsRepo = evaluationsRepo;
        this.userRepo = userRepo;
        this.bookingHistoryRepo = bookingHistoryRepo;
        this.bookingRepo = bookingRepo;
        this.accommodationRepo = accommodationRepo;
    }

    @GetMapping(value = "/ertekelesek_admin")
    public String evaluationAdminHandler(Model model) {
        // if nobody logged in
        if (UserController.getNobodyIsLoggedIn()) {
            return "/log_reg";
        }

        // Add the list of evaluations to the model
        model.addAttribute("ertekelesek", evaluationsRepo.listOfTheEvaluations());
        return "evaluations_admin";
    }

    @PostMapping(value = "/ertekelesek_admin/{id}")
    public String evaluationDelete(
            @PathVariable("id") Integer id
    ) {
        // go through all the evaluation list and if
        // find this evaluation by id and then delete it from the database
        for (Evaluations evaluation : evaluationsRepo.listOfTheEvaluations()) {
            if (Objects.equals(evaluation.getId(), id)) {
                evaluationsRepo.delete(evaluation);
                break;
            }
        }
        return "redirect:/ertekelesek_admin";
    }

    @GetMapping(value = "/szallas/{id}/ertekel")
    public String evaluation(
            @PathVariable("id") int id,
            Model model, RedirectAttributes redirectAttributes
    ) {
        // Check if any user is logged in
        nobodyLoggedIn = UserController.getNobodyIsLoggedIn();
        userIsAdmin = UserController.getAdminIsLoggedIn();
        userIsHost = UserController.getRentalIsLoggedIn();
        userIsRenter = UserController.getBookingIsLoggedIn();

        // If no user is logged in, redirect to login/registration page
        if (nobodyLoggedIn) {
            redirectAttributes.addFlashAttribute("Uzenet", "Előbb kell jelentkezned, hogy értékelhess szállásokat!");
            return "redirect:/log_reg";
        }

        // Add user roles and login status for the menu
        model.addAttribute("berlo", userIsRenter);
        model.addAttribute("ures", nobodyLoggedIn);
        model.addAttribute("kiado", userIsHost);
        model.addAttribute("admin", userIsAdmin);

        // Retrieve the accommodation by its ID and add it to the model
        for (Accommodation accommodation : accommodationRepo.listOfTheAccommodation())
            if (accommodation.getId() == id)
                model.addAttribute("szallas", accommodation);

        // Return the view for reviewing an accommodation
        return "evaluation";
    }

    @PostMapping(value = "/szallas/{id}/ertekel")
    public String submitReview(
            @PathVariable("id") int accommodationId,
            @RequestParam("text") String reviewText,
            @RequestParam("ertekeles") int rating
    ) {
        // Retrieve the currently logged-in user
        loggedInUser = UserController.getLogginInUser();

        // current date
        Instant ido = Instant.now();

        // create evaluation for the accommodation
        Evaluations newEvaluation = new Evaluations(
                accommodationRepo.findById(accommodationId).orElse(null),
                loggedInUser,
                ido,
                rating,
                reviewText
        );

        // Save the new review to the repository
        evaluationsRepo.save(newEvaluation);

        // Add the new review to the list of reviews
        evaluationsRepo.listOfTheEvaluations().add(newEvaluation);

        // Redirect back to the accommodation details page
        return "redirect:/szallas/{id}";
    }

    @GetMapping(value = "/szallas/{id}/ertekelesek")
    public String viewReviews(
            @PathVariable("id") int accommodationId,
            Model model
    ) {
        // Check user status for menu visibility
        nobodyLoggedIn = UserController.getNobodyIsLoggedIn();
        userIsAdmin = UserController.getAdminIsLoggedIn();
        userIsHost = UserController.getRentalIsLoggedIn();
        userIsRenter = UserController.getBookingIsLoggedIn();

        // Add user status attributes to the model for the view
        model.addAttribute("berlo", userIsRenter);
        model.addAttribute("ures", nobodyLoggedIn);
        model.addAttribute("kiado", userIsHost);
        model.addAttribute("admin", userIsAdmin);

        // Retrieve the accommodation by ID
        Accommodation accommodation = accommodationRepo.findById(accommodationId).orElse(null);
        model.addAttribute("szallas", accommodation);

        // Retrieve reviews for the accommodation
        List<Evaluations> reviews = new ArrayList<>();

        for (Evaluations review : evaluationsRepo.listOfTheEvaluations()) {
            // Add reviews associated with the accommodation
            if (review.getAccommodationID().getId() == accommodationId) {
                reviews.add(review);
            }
        }

        // Add the list of reviews to the model
        model.addAttribute("ertekelesek", reviews);

        // Return the view for displaying reviews
        return "evaluations";
    }
}
