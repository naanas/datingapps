package com.dating.apps.datingapps.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    private String fullName;
    private String gender; // "MALE" or "FEMALE"
    private String bio;
    
    // Kita simpan URL foto nanti disini jika perlu
    private String photoUrl; 

    // Koordinat GPS (Wajib diisi dari Frontend)
    private Double latitude;
    private Double longitude;
}