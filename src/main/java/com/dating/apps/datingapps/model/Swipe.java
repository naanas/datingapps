package com.dating.apps.datingapps.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "swipes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"swiperId", "targetId"})
})
public class Swipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID swiperId;
    private UUID targetId;
    
    private String actionType; // "LIKE" or "PASS"
    
    private LocalDateTime createdAt = LocalDateTime.now();
}