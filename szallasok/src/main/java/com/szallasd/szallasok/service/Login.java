package com.szallasd.szallasok.service;

import com.szallasd.szallasok.entity.User;
import com.szallasd.szallasok.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public class Login {
    // for the @Autowired warning
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private final UserRepo userRepo;

    public Login(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public String userLogin(String email, String password) {
        String message = "";
        // check if the given email is in the users list
        // (there are registered an account with this email)
        for (User userElement : userRepo.listOfTheUsers()) {
            if (Objects.equals(userElement.getEmail(), email)) {
                message = "Van email";
            }
            if (Objects.equals(userElement.getPassword(), password) && Objects.equals(userElement.getEmail(), email)) {
                // if the email found and the password is correct then
                // it will return with this string and show on the html area later
                message = "Sikeres belépés";
                break;
            }
        }
        return message;
    }

    public User findUserByEmail(String email) {
        User foundUser = new User("", "", "", "", "", "");
        // go through the users list
        // and if found a user with this email then return with this user
        for (User user : userRepo.listOfTheUsers()) {
            if (user.getEmail().equals(email)) {
                foundUser.setEmail(user.getEmail());
                foundUser.setUserType(user.getUserType());
                foundUser.setName(user.getName());
                foundUser.setAddress(user.getAddress());
                foundUser.setPhone(user.getPhone());
                foundUser.setPassword(user.getPassword());
            }
        }
        return foundUser;
    }
}
