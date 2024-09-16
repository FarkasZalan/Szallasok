package com.szallasd.szallasok.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.Instant;

@Entity
// Evaluations table is 'viewReviews'
@Table(name = "viewReviews")
public class Evaluations {
    // I used the viewReviews ID as a generated number which is automatically increases
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluationsID", nullable = false)
    private Integer id;

    // I create a connection between the 'accommodation' table and the 'viewReviews' table
    // the connection is the accommodationID
    // and if the item is deleted from the table then it will be deleted from the other one too
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "accommodationID", nullable = false)
    private Accommodation accommodationID;

    // same like the Accommodation but here the
    // connection is the rental user email address
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "evaluationUser", nullable = false)
    private User evaluationUser;

    // this column will store the evaluation's date
    @Column(name = "evaluationDate", nullable = false)
    private Instant evaluationDate;

    // this column will store the evaluation's value
    @Column(name = "evaluation", nullable = false)
    private Integer evaluation;

    // this column will store the evaluation's description
    @Lob
    @Column(name = "evaluationDescription", nullable = false)
    private String evaluationDescription;

    public Evaluations() {
    }

    public Evaluations(Accommodation accommodationID, User evaluationUser, Instant evaluationDate, Integer evaluation, String evaluationDescription) {
        this.accommodationID = accommodationID;
        this.evaluationUser = evaluationUser;
        this.evaluationDate = evaluationDate;
        this.evaluation = evaluation;
        this.evaluationDescription = evaluationDescription;
    }

    public String getEvaluationDescription() {
        return evaluationDescription;
    }

    public void setEvaluationDescription(String ertekelesLeiras) {
        this.evaluationDescription = ertekelesLeiras;
    }

    public Integer getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Integer ertekeles) {
        this.evaluation = ertekeles;
    }

    public Instant getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(Instant ertekelesIdopontja) {
        this.evaluationDate = ertekelesIdopontja;
    }

    public User getEvaluationUser() {
        return evaluationUser;
    }

    public void setEvaluationUser(User ertekeloEmailCime) {
        this.evaluationUser = ertekeloEmailCime;
    }

    public Accommodation getAccommodationID() {
        return accommodationID;
    }

    public void setAccommodationID(Accommodation accommodationID) {
        this.accommodationID = accommodationID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}