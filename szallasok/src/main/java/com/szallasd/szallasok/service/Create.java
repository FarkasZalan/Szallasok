package com.szallasd.szallasok.service;

import com.szallasd.szallasok.entity.User;
import com.szallasd.szallasok.repository.AccommodationRepo;
import com.szallasd.szallasok.repository.UserRepo;
import com.szallasd.szallasok.repository.BookingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class Create {
    @Autowired
    private final UserRepo userRepo;

    public Create(UserRepo userRepo, BookingRepo bookingRepo, AccommodationRepo accommodationRepo) {
        this.userRepo = userRepo;
    }

    public String createUser(User user) {
        // Validate the email format
        if (!(user.getEmail().contains("@") && (user.getEmail().contains(".")))) {
            return "Érvénytelen email cím, az email címben kell lennie '@'-nak és egy érvényes domainnek";
        }

        // Check if the email is already registered
        boolean emailExists = false;

        for (User existingUser : userRepo.listOfTheUsers()) {
            if (Objects.equals(user.getEmail(), existingUser.getEmail())) {
                emailExists = true;
                break;
            }
        }
        // Save the user if email is not already registered
        if (!emailExists) {
            userRepo.save(user);
            return "Sikeres mentés!";
        }
        return "Ezzel az email címmel már regisztráltak!";
    }
}
