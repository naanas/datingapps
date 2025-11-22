package com.dating.apps.datingapps.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dating.apps.datingapps.model.*;
import com.dating.apps.datingapps.repository.*;

@Service
public class DatingService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SwipeRepository swipeRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- 1. HELPER (PENTING: Ini yang tadi hilang) ---
    public User getUserByEmail(String email) {
        return Objects.requireNonNull(
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User tidak ditemukan")));
    }

    // --- AUTH ---
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return Objects.requireNonNull(userRepository.save(user));
    }

    public User login(String email, String rawPassword) {
        User user = getUserByEmail(email); // Pakai helper biar konsisten

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Password salah");
        }
        return user;
    }

    public User updateProfile(UUID userId, User updatedData) {
        UUID safeId = Objects.requireNonNull(userId);

        User existing = Objects.requireNonNull(
                userRepository.findById(safeId)
                        .orElseThrow(() -> new RuntimeException("User not found")));

        if (updatedData.getBio() != null)
            existing.setBio(updatedData.getBio());
        if (updatedData.getJobTitle() != null)
            existing.setJobTitle(updatedData.getJobTitle());
        if (updatedData.getCompany() != null)
            existing.setCompany(updatedData.getCompany());
        if (updatedData.getPhotoUrl() != null)
            existing.setPhotoUrl(updatedData.getPhotoUrl());
        if (updatedData.getDateOfBirth() != null)
            existing.setDateOfBirth(updatedData.getDateOfBirth());

        return Objects.requireNonNull(userRepository.save(existing));
    }

    // --- DATING ---
    public List<User> getFeed(UUID myId, double radiusKm) {
        UUID safeId = Objects.requireNonNull(myId);

        User me = Objects.requireNonNull(
                userRepository.findById(safeId)
                        .orElseThrow(() -> new RuntimeException("User not found")));

        if (me.getGender() == null || me.getLatitude() == null || me.getLongitude() == null) {
            return List.of();
        }

        String targetGender = "MALE".equalsIgnoreCase(me.getGender()) ? "FEMALE" : "MALE";

        return Objects.requireNonNull(
                userRepository.findPotentialMatches(
                        safeId, targetGender, me.getLatitude(), me.getLongitude(), radiusKm * 1000));
    }

    public boolean swipeUser(UUID myId, UUID targetId, String action) {
        UUID safeMyId = Objects.requireNonNull(myId);
        UUID safeTargetId = Objects.requireNonNull(targetId);

        Swipe swipe = new Swipe();
        swipe.setSwiperId(safeMyId);
        swipe.setTargetId(safeTargetId);
        swipe.setActionType(action);
        swipeRepository.save(swipe);

        if ("PASS".equalsIgnoreCase(action))
            return false;

        boolean isMatch = swipeRepository
                .findBySwiperIdAndTargetIdAndActionType(safeTargetId, safeMyId, "LIKE")
                .isPresent();

        if (isMatch)
            createMatch(safeMyId, safeTargetId);
        return isMatch;
    }

    private void createMatch(UUID user1, UUID user2) {
        Match match = new Match();
        match.setUser1Id(user1);
        match.setUser2Id(user2);
        matchRepository.save(match);
    }

    public List<Match> getMyMatches(UUID userId) {
        return Objects.requireNonNull(
                matchRepository.findAllByUserId(Objects.requireNonNull(userId)));
    }

    // --- CHAT (SECURE LOGIC) ---
    // FIX: Method ini dikembalikan lagi validasinya
    public Message sendMessage(Long matchId, UUID senderId, String content) {
        Match match = Objects.requireNonNull(matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match tidak ditemukan")));

        // Validasi Security
        if (!match.getUser1Id().equals(senderId) && !match.getUser2Id().equals(senderId)) {
            throw new RuntimeException("Kamu bukan pasangan di match ini!");
        }

        Message msg = new Message();
        msg.setMatchId(matchId);
        msg.setSenderId(Objects.requireNonNull(senderId));
        msg.setContent(content);

        return Objects.requireNonNull(messageRepository.save(msg));
    }

    // FIX: Method ini menerima 2 parameter agar cocok dengan Controller
    public List<Message> getChatHistory(Long matchId, UUID requestingUserId) {
        Match match = Objects.requireNonNull(matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match tidak ditemukan")));

        // Validasi Security
        if (!match.getUser1Id().equals(requestingUserId) && !match.getUser2Id().equals(requestingUserId)) {
            throw new RuntimeException("Dilarang mengintip chat orang lain!");
        }

        return Objects.requireNonNull(
                messageRepository.findByMatchIdOrderBySentAtAsc(matchId));
    }
}