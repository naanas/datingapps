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

    public User registerUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getFeed(UUID myId, double radiusKm) {
        User me = userRepository.findById(myId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String targetGender = "MALE".equalsIgnoreCase(me.getGender()) ? "FEMALE" : "MALE";
        double radiusMeters = radiusKm * 1000;

        return userRepository.findPotentialMatches(
            myId, 
            targetGender, 
            me.getLatitude(), 
            me.getLongitude(), 
            radiusMeters
        );
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