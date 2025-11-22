package com.dating.apps.datingapps.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;
    private String password; // Password simple

    private String fullName;
    private String gender;
    private String bio;

    private LocalDate dateOfBirth;
    private String jobTitle;
    private String company;
    private String photoUrl;

    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "geography(Point, 4326)")
    private Object location;

    private LocalDateTime lastActive = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    public Integer getAge() {
        if (dateOfBirth == null)
            return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}