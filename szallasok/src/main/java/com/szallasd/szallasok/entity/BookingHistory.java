package com.szallasd.szallasok.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
// Booking history table is 'booking_history'
@Table(name = "booking_history")
public class BookingHistory {
    // I used the booking history ID as a generated number which is automatically increases
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_history_id", nullable = false)
    private Integer id;

    // I create a connection between the 'user' table and the 'booking_history' table
    // the connection is the loggedUser
    // and if the item is deleted from the table then it will be deleted from the other one too
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "logged_user", nullable = false)
    private User loggedUser;

    // this column will store the exact date when the user booked the accommodation
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    // this column will store the booking ending date
    @Column(name = "booking_emd_date", nullable = false)
    private LocalDate bookingEndDate;

    public LocalDate getBookingEndDate() {
        return bookingEndDate;
    }

    public void setBookingEndDate(LocalDate bookingEndDate) {
        this.bookingEndDate = bookingEndDate;
    }

    public LocalDate getBookingStartDate() {
        return bookingStartDate;
    }

    public void setBookingStartDate(LocalDate kezdo_idopontja) {
        this.bookingStartDate = kezdo_idopontja;
    }

    // this column will store the booking starting date
    @Column(name = "booking_start_date", nullable = false)
    private LocalDate bookingStartDate;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "person_quantity", nullable = false)
    private Integer personQuantity;

    @Column(name = "day_quantity", nullable = false)
    private Integer dayQuantity;

    public String getAccommodationName() {
        return accommodationName;
    }

    public void setAccommodationName(String accommodationName) {
        this.accommodationName = accommodationName;
    }

    // this column will store the name of the accommodation the user booked
    @Column(name = "accommodation_name", nullable = false)
    private String accommodationName;

    public String getAccommodationImage() {
        return accommodationImage;
    }

    public void setAccommodationImage(String szallasKepe) {
        this.accommodationImage = szallasKepe;
    }

    @Lob
    @Column(name = "accommodation_image", nullable = false)
    private String accommodationImage;

    public String getRentalUserEmail() {
        return rentalUserEmail;
    }

    public void setRentalUserEmail(String kiadoEmail) {
        this.rentalUserEmail = kiadoEmail;
    }

    @Column(name = "rentalUserEmail", nullable = false)
    private String rentalUserEmail;

    @Column(name = "accommodationID", nullable = false)
    private Integer accommodationID;

    public BookingHistory() {
    }

    public BookingHistory(User loggedUser, LocalDate bookingDate, LocalDate bookingStartDate, LocalDate bookingEndDate, Integer totalAmount, Integer personQuantity, Integer dayQuantity, String accommodationName, Integer accommodationID, String accommodationImage, String rentalUserEmail) {
        this.loggedUser = loggedUser;
        this.bookingDate = bookingDate;
        this.bookingStartDate = bookingStartDate;
        this.bookingEndDate = bookingEndDate;
        this.totalAmount = totalAmount;
        this.personQuantity = personQuantity;
        this.dayQuantity = dayQuantity;
        this.accommodationName = accommodationName;
        this.accommodationID = accommodationID;
        this.accommodationImage = accommodationImage;
        this.rentalUserEmail = rentalUserEmail;
    }

    public Integer getDayQuantity() {
        return dayQuantity;
    }

    public void setDayQuantity(Integer dayQuantity) {
        this.dayQuantity = dayQuantity;
    }

    public Integer getPersonQuantity() {
        return personQuantity;
    }

    public void setPersonQuantity(Integer personQuantity) {
        this.personQuantity = personQuantity;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public Integer getAccommodationId() {
        return accommodationID;
    }

    public void setAccomodationID(Integer accommodationID) {
        this.accommodationID = accommodationID;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public User getLoggedInUser() {
        return loggedUser;
    }

    public void setLoggedInUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}