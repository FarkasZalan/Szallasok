package com.szallasd.szallasok.repository;

import com.szallasd.szallasok.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface UserRepo extends JpaRepository<User, String> {
    // Retrieves all users from the database.
    @Query(value = "select * from user", nativeQuery = true)
    List<User> listOfTheUsers();

    // Updates the details of a specific user identified by user email address.
    // Parameters include email name, phone, address and password
    @Modifying
    @Query(value="update user set name= :name, phone= :phone, address= :address, password= :password where email = :email", nativeQuery = true)
    void updateUser(@Param("name") String name,
                    @Param("phone") String phone,
                    @Param("address") String address,
                    @Param("password") String password,
                    @Param("email") String email
    );
}
