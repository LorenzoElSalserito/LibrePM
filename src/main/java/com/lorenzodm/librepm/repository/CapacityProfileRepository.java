package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.CapacityProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CapacityProfileRepository extends JpaRepository<CapacityProfile, String> {

    List<CapacityProfile> findByUserId(String userId);

    @Query("SELECT cp FROM CapacityProfile cp WHERE cp.user.id = :userId " +
           "AND cp.effectiveFrom <= :date AND (cp.effectiveTo IS NULL OR cp.effectiveTo >= :date)")
    Optional<CapacityProfile> findActiveForUserOnDate(String userId, LocalDate date);
}
