package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.RateCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RateCardRepository extends JpaRepository<RateCard, String> {

    List<RateCard> findByScopeAndScopeEntityId(RateCard.Scope scope, String scopeEntityId);

    @Query("SELECT rc FROM RateCard rc WHERE rc.scope = :scope AND rc.scopeEntityId = :entityId " +
           "AND rc.effectiveFrom <= :date AND (rc.effectiveTo IS NULL OR rc.effectiveTo >= :date)")
    Optional<RateCard> findActiveForScopeOnDate(RateCard.Scope scope, String entityId, LocalDate date);
}
