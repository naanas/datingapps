package com.dating.apps.datingapps.service;

import java.util.List;
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

    @SuppressWarnings("null")
    public User registerUser(User user) {
        // Enkripsi Password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @SuppressWarnings("null")
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan"));

        // Cek Password (Raw vs Encrypted)
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Password salah");
        }
        return user;
    }

    @SuppressWarnings("null")
    public User updateProfile(UUID userId, User updatedData) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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

        return userRepository.save(existing);
    }

    @SuppressWarnings("null")
    public List<User> getFeed(UUID myId, double radiusKm) {
        User me = userRepository.findById(myId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (me.getGender() == null || me.getLatitude() == null || me.getLongitude() == null) {
            return List.of();
        }

        String targetGender = "MALE".equalsIgnoreCase(me.getGender()) ? "FEMALE" : "MALE";

        return userRepository.findPotentialMatches(
                myId, targetGender, me.getLatitude(), me.getLongitude(), radiusKm * 1000);
    }

    @SuppressWarnings("null")
    public boolean swipeUser(UUID myId, UUID targetId, String action) {
        Swipe swipe = new Swipe();
        swipe.setSwiperId(myId);
        swipe.setTargetId(targetId);
        swipe.setActionType(action);
        swipeRepository.save(swipe);

        if ("PASS".equalsIgnoreCase(action))
            return false;

        boolean isMatch = swipeRepository
                .findBySwiperIdAndTargetIdAndActionType(targetId, myId, "LIKE")
                .isPresent();

        if (isMatch)
            createMatch(myId, targetId);
        return isMatch;
    }

    private void createMatch(UUID user1, UUID user2) {
        Match match = new Match();
        match.setUser1Id(user1);
        match.setUser2Id(user2);
        matchRepository.save(match);
    }

    public List<Match> getMyMatches(UUID userId) {
        return matchRepository.findAllByUserId(userId);
    }

    @SuppressWarnings("null")
    public Message sendMessage(Long matchId, UUID senderId, String content) {
        Message msg = new Message();
        msg.setMatchId(matchId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        return messageRepository.save(msg);
    }

    public List<Message> getChatHistory(Long matchId) {
        return messageRepository.findByMatchIdOrderBySentAtAsc(matchId);
    }
}