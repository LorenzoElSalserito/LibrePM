package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.RateCard;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RateCardService {

    List<RateCard> listByScope(RateCard.Scope scope, String entityId);
    RateCard create(RateCard rateCard);
    RateCard update(String id, RateCard rateCard);
    void delete(String id);

    /**
     * Resolves the effective rate for a user on a given date.
     * Precedence: USER > PROJECT > ROLE
     */
    Optional<RateCard> resolveRate(String userId, String projectId, String roleId, LocalDate date);
}
