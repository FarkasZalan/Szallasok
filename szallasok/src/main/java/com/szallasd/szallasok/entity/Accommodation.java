package com.szallasd.szallasok.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
// Accommodation table is 'accommodation'
@Table(name = "accommodation")
public class Accommodation {
    // I used the accommodations ID as a generated number which is automatically increases
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accommodationID", nullable = false)
    private Integer id;

    // this column will store the evaluation's name
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    // I create a connection between the 'user' table and the 'viewReviews' table
    // the connection is the rental email address
    // and if the item is deleted from the table then it will be deleted from the other one too
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "rental_user", nullable = false)
    private User rentalUser;

    // this column will store the evaluation's location
    @Column(name = "location", nullable = false, length = 200)
    private String location;

    // this column will store the evaluation's description
    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    public String getImage() {
        return image;
    }

    public void setImage(String kep) {
        this.image = kep;
    }

    // this column will store the evaluation's image
    @Lob
    @Column(name = "image", nullable = false, columnDefinition = "MEDIUMBLOB")
    private String image;

    // this column will store the evaluation's prie/day/person
    @Column(name = "price_per_day_per_person", nullable = false)
    private Integer pricePerDayPerPerson;

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxFerohely) {
        this.maxCapacity = maxFerohely;
    }

    // this column will store the evaluation's maximum capacity
    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    public Accommodation() {
    }

    public Accommodation(String name, User kiadoEmail, String location, String description, Integer pricePerDayPerPerson, Integer maxCapacity, String image) {
        this.name = name;
        this.rentalUser = kiadoEmail;
        this.location = location;
        this.description = description;
        this.pricePerDayPerPerson = pricePerDayPerPerson;
        this.maxCapacity = maxCapacity;
        this.image = image;
    }

    public Integer getPricePerDayPerPerson() {
        return pricePerDayPerPerson;
    }

    public void setPricePerDayPerPerson(Integer arPerNapPerFo) {
        this.pricePerDayPerPerson = arPerNapPerFo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String leiras) {
        this.description = leiras;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String helyszin) {
        this.location = helyszin;
    }

    public User getRentalUser() {
        return rentalUser;
    }

    public void setRentalUser(User kiadoEmail) {
        this.rentalUser = kiadoEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String nev) {
        this.name = nev;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}