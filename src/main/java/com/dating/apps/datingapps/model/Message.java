package com.dating.apps.datingapps.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long matchId; // Chat ini milik Match ID berapa
    private UUID senderId; // Siapa yang kirim

    private String content; // Isi chat
    private LocalDateTime sentAt = LocalDateTime.now();
}