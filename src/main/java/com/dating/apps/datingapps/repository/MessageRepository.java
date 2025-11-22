package com.dating.apps.datingapps.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dating.apps.datingapps.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByMatchIdOrderBySentAtAsc(Long matchId);
}