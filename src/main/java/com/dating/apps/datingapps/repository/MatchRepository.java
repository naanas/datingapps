package com.dating.apps.datingapps.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dating.apps.datingapps.model.Match;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    // Cari match yang melibatkan user tertentu
    @Query("SELECT m FROM Match m WHERE m.user1Id = :userId OR m.user2Id = :userId")
    List<Match> findAllByUserId(UUID userId);
}