package com.dating.apps.datingapps.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dating.apps.datingapps.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // PERBAIKAN:
    // 1. Menggunakan u.longitude & u.latitude karena kolom u.location tidak ada di
    // Entity.
    // 2. Menggunakan casting ::geography agar radius dihitung dalam METER, bukan
    // DERAJAT.
    @Query(value = """
                SELECT * FROM users u
                WHERE u.id != :myId
                AND u.gender = :targetGender
                AND u.id NOT IN (
                    SELECT s.target_id FROM swipes s WHERE s.swiper_id = :myId
                )
                AND ST_DWithin(
                    ST_SetSRID(ST_MakePoint(u.longitude, u.latitude), 4326)::geography,
                    ST_SetSRID(ST_MakePoint(:myLong, :myLat), 4326)::geography,
                    :radiusInMeters
                )
                LIMIT 20
            """, nativeQuery = true)
    List<User> findPotentialMatches(
            @Param("myId") UUID myId,
            @Param("targetGender") String targetGender,
            @Param("myLat") Double myLat,
            @Param("myLong") Double myLong,
            @Param("radiusInMeters") double radiusInMeters);
}