package com.dating.apps.datingapps.controller;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.dating.apps.datingapps.config.JwtUtil;
import com.dating.apps.datingapps.model.*;
import com.dating.apps.datingapps.service.DatingService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Izinkan akses dari semua origin (untuk development)
public class DatingController {

    @Autowired
    private DatingService datingService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    // --- MODIFIKASI DISINI: REGISTER MENERIMA LOKASI ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // Log sederhana untuk memastikan Latitude & Longitude masuk
        System.out.println("Registering User: " + user.getEmail());
        System.out.println("Location received: " + user.getLatitude() + ", " + user.getLongitude());

        try {
            User savedUser = datingService.registerUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String password = payload.get("password");

            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(email, password));

            if (authentication.isAuthenticated()) {
                String token = jwtUtil.generateToken(email);
                User userDetails = datingService.getUserByEmail(email);

                // Update last active saat login
                userDetails.setLastActive(LocalDateTime.now());
                datingService.updateProfile(userDetails.getId(), userDetails);

                return ResponseEntity.ok(Map.of("token", token, "user", userDetails));
            } else {
                return ResponseEntity.status(401).body("Login Gagal");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Email atau Password Salah!"));
        }
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<User> updateProfile(@PathVariable UUID userId, @RequestBody User user) {
        return ResponseEntity.ok(datingService.updateProfile(userId, user));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file) {
        try {
            // Pastikan folder uploads ada
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate nama file unik
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Simpan file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL yang bisa diakses (sesuaikan dengan config static resource jika
            // perlu)
            return ResponseEntity.ok(Map.of("url", "uploads/" + fileName));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload gagal: " + e.getMessage());
        }
    }

    // --- LOGIC FEED MENGGUNAKAN LOKASI DARI DATABASE ---
    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(@RequestParam UUID userId,
            @RequestParam(defaultValue = "50.0") double radiusKm) {
        try {
            List<User> matches = datingService.getFeed(userId, radiusKm);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            // Jika user belum punya lokasi atau error lain
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/swipe")
    public ResponseEntity<?> swipe(@RequestBody Map<String, String> payload) {
        try {
            UUID myId = UUID.fromString(payload.get("myId"));
            UUID targetId = UUID.fromString(payload.get("targetId"));
            String action = payload.get("action");

            boolean isMatch = datingService.swipeUser(myId, targetId, action);

            return ResponseEntity.ok(
                    Map.of("status", isMatch ? "MATCH" : "OK",
                            "message", isMatch ? "It's a Match!" : "Swipe recorded"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/matches")
    public ResponseEntity<List<Match>> getMatches(@RequestParam UUID userId) {
        return ResponseEntity.ok(datingService.getMyMatches(userId));
    }

    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> payload, Principal principal) {
        try {
            // Ambil user pengirim dari token JWT
            User sender = datingService.getUserByEmail(principal.getName());

            Long matchId = Long.valueOf(payload.get("matchId").toString());
            String content = (String) payload.get("content");

            return ResponseEntity.ok(datingService.sendMessage(matchId, sender.getId(), content));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/messages/{matchId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long matchId, Principal principal) {
        try {
            User requester = datingService.getUserByEmail(principal.getName());
            return ResponseEntity.ok(datingService.getChatHistory(matchId, requester.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}