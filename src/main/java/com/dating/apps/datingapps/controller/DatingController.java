package com.dating.apps.datingapps.controller;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.dating.apps.datingapps.model.*;
import com.dating.apps.datingapps.service.DatingService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DatingController {

    @Autowired
    private DatingService datingService;

    // --- AUTH ---
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(datingService.registerUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        try {
            User user = datingService.login(payload.get("email"), payload.get("password"));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // --- PROFILE & UPLOAD ---
    @PutMapping("/profile/{userId}")
    public ResponseEntity<User> updateProfile(@PathVariable UUID userId, @RequestBody User user) {
        return ResponseEntity.ok(datingService.updateProfile(userId, user));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath))
                Files.createDirectories(uploadPath);

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(Map.of("url", "uploads/" + fileName));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload gagal: " + e.getMessage());
        }
    }

    // --- DATING ---
    @GetMapping("/feed")
    public ResponseEntity<List<User>> getFeed(@RequestParam UUID userId,
            @RequestParam(defaultValue = "50.0") double radiusKm) {
        return ResponseEntity.ok(datingService.getFeed(userId, radiusKm));
    }

    @PostMapping("/swipe")
    public ResponseEntity<?> swipe(@RequestBody Map<String, String> payload) {
        UUID myId = UUID.fromString(payload.get("myId"));
        UUID targetId = UUID.fromString(payload.get("targetId"));
        String action = payload.get("action");
        boolean isMatch = datingService.swipeUser(myId, targetId, action);
        return ResponseEntity.ok(
                Map.of("status", isMatch ? "MATCH" : "OK", "message", isMatch ? "It's a Match!" : "Swipe recorded"));
    }

    // --- CHAT ---
    @GetMapping("/matches")
    public ResponseEntity<List<Match>> getMatches(@RequestParam UUID userId) {
        return ResponseEntity.ok(datingService.getMyMatches(userId));
    }

    @PostMapping("/messages")
    public ResponseEntity<Message> sendMessage(@RequestBody Map<String, Object> payload) {
        Long matchId = Long.valueOf(payload.get("matchId").toString());
        UUID senderId = UUID.fromString(payload.get("senderId").toString());
        String content = (String) payload.get("content");
        return ResponseEntity.ok(datingService.sendMessage(matchId, senderId, content));
    }

    @GetMapping("/messages/{matchId}")
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable Long matchId) {
        return ResponseEntity.ok(datingService.getChatHistory(matchId));
    }
}