package com.dating.apps.datingapps.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dating.apps.datingapps.model.Swipe;

@Repository
public interface SwipeRepository extends JpaRepository<Swipe, Long> {
    Optional<Swipe> findBySwiperIdAndTargetIdAndActionType(UUID swiperId, UUID targetId, String actionType);
}