package com.dating.apps.datingapps.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dating.apps.datingapps.model.Swipe;
import com.dating.apps.datingapps.model.User;
import com.dating.apps.datingapps.repository.SwipeRepository;
import com.dating.apps.datingapps.repository.UserRepository;

@Service
public class DatingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SwipeRepository swipeRepository;

    // PERBAIKAN 1: Menambahkan SuppressWarnings agar garis kuning di VS Code hilang
    @SuppressWarnings("null")
    public User registerUser(User user) {
        return userRepository.save(user);
    }

    // PERBAIKAN 2: Update logika getFeed (seperti instruksi sebelumnya)
    // agar tidak error Null Pointer dan radiusnya akurat
    public List<User> getFeed(UUID myId, double radiusKm) {
        // Cek input ID
        if (myId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User me = userRepository.findById(myId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Hindari error jika data user belum lengkap (misal baru daftar tapi belum set
        // lokasi)
        if (me.getGender() == null || me.getLatitude() == null || me.getLongitude() == null) {
            return List.of();
        }

        String targetGender = "MALE".equalsIgnoreCase(me.getGender()) ? "FEMALE" : "MALE";
        double radiusMeters = radiusKm * 1000;

        return userRepository.findPotentialMatches(
                myId,
                targetGender,
                me.getLatitude(),
                me.getLongitude(),
                radiusMeters);
    }

    public boolean swipeUser(UUID myId, UUID targetId, String action) {
        Swipe swipe = new Swipe();
        swipe.setSwiperId(myId);
        swipe.setTargetId(targetId);
        swipe.setActionType(action);
        swipeRepository.save(swipe);

        if ("PASS".equalsIgnoreCase(action)) {
            return false;
        }

        return swipeRepository
                .findBySwiperIdAndTargetIdAndActionType(targetId, myId, "LIKE")
                .isPresent();
    }
}