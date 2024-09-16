package com.szallasd.szallasok.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
// Bookings table is 'booking'
@Table(name = "booking")
public class Booking {
    // I used the bookings ID as a generated number which is automatically increases
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookingID", nullable = false)
    private Integer id;

    // I create a connection between the 'accommodation' table and the 'booking' table
    // the connection is the accommodationID
    // and if the item is deleted from the table then it will be deleted from the other one too
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "accommodationID", nullable = false)
    private Accommodation accommodationID;

    // same like the Accommodation but
    // here the connection is the renter user email address
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "booking_user", nullable = false)
    private User bookingUser;

    // this column will store the booking's date
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "starting_date", nullable = false)
    private LocalDate startingDate;

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate foglalas_idopontja) {
        this.bookingDate = foglalas_idopontja;
    }

    public LocalDate getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(LocalDate kezdo_idopontja) {
        this.startingDate = kezdo_idopontja;
    }

    public LocalDate getEndingDate() {
        return endingDate;
    }

    public void setEndingDate(LocalDate vegzo_idopontja) {
        this.endingDate = vegzo_idopontja;
    }

    @Column(name = "ending_date", nullable = false)
    private LocalDate endingDate;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "person_quantity", nullable = false)
    private Integer personQuantity;

    @Column(name = "day_quantity", nullable = false)
    private Integer dayQuantity;

    public Booking() {
    }

    public Booking(Accommodation accommodationID, User bookingUser, LocalDate bookingDate, LocalDate startingDate, LocalDate endingDate, Integer totalAmount, Integer personQuantity, Integer dayQuantity) {
        this.accommodationID = accommodationID;
        this.bookingUser = bookingUser;
        this.bookingDate = bookingDate;
        this.startingDate = startingDate;
        this.endingDate = endingDate;
        this.totalAmount = totalAmount;
        this.personQuantity = personQuantity;
        this.dayQuantity = dayQuantity;
    }

    public Integer getDayQuantity() {
        return dayQuantity;
    }

    public void setDayQuantity(Integer napMennyiseg) {
        this.dayQuantity = napMennyiseg;
    }

    public Integer getPersonQuantity() {
        return personQuantity;
    }

    public void setPersonQuantity(Integer szemelyMennyiseg) {
        this.personQuantity = szemelyMennyiseg;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer fizetendoOsszeg) {
        this.totalAmount = fizetendoOsszeg;
    }

    public LocalDate getFoglalas() {
        return bookingDate;
    }

    public void setFoglalas(LocalDate foglalas) {
        this.bookingDate = foglalas;
    }

    public User getBookingUser() {
        return bookingUser;
    }

    public void setBookingUser(User foglaloEmailCime) {
        this.bookingUser = foglaloEmailCime;
    }

    public Accommodation getAccommodationID() {
        return accommodationID;
    }

    public void setAccommodationID(Accommodation szallasID) {
        this.accommodationID = szallasID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}