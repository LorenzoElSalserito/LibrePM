package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.CalendarFeedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalendarFeedTokenRepository extends JpaRepository<CalendarFeedToken, String> {

    Optional<CalendarFeedToken> findByUserId(String userId);

    Optional<CalendarFeedToken> findByToken(String token);

    boolean existsByUserId(String userId);
}
