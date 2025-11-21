package com.dating.apps.datingapps.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dating.apps.datingapps.model.User;
import com.dating.apps.datingapps.service.DatingService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DatingController {

    @Autowired
    private DatingService datingService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(datingService.registerUser(user));
    }

    @GetMapping("/feed")
    public ResponseEntity<List<User>> getFeed(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "50.0") double radiusKm) {
        return ResponseEntity.ok(datingService.getFeed(userId, radiusKm));
    }

    @PostMapping("/swipe")
    public ResponseEntity<?> swipe(@RequestBody Map<String, String> payload) {
        UUID myId = UUID.fromString(payload.get("myId"));
        UUID targetId = UUID.fromString(payload.get("targetId"));
        String action = payload.get("action"); 

        boolean isMatch = datingService.swipeUser(myId, targetId, action);

        if (isMatch) {
            return ResponseEntity.ok(Map.of("status", "MATCH", "message", "It's a Match!"));
        } else {
            return ResponseEntity.ok(Map.of("status", "OK", "message", "Swipe recorded."));
        }
    }
}