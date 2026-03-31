package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.RateCard;
import com.lorenzodm.librepm.repository.RateCardRepository;
import com.lorenzodm.librepm.service.RateCardService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RateCardServiceImpl implements RateCardService {

    private final RateCardRepository rateCardRepo;

    public RateCardServiceImpl(RateCardRepository rateCardRepo) {
        this.rateCardRepo = rateCardRepo;
    }

    @Override
    public List<RateCard> listByScope(RateCard.Scope scope, String entityId) {
        return rateCardRepo.findByScopeAndScopeEntityId(scope, entityId);
    }

    @Override
    public RateCard create(RateCard rateCard) {
        return rateCardRepo.save(rateCard);
    }

    @Override
    public RateCard update(String id, RateCard updated) {
        RateCard existing = rateCardRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RateCard not found: " + id));
        if (updated.getScope() != null) existing.setScope(updated.getScope());
        if (updated.getScopeEntityId() != null) existing.setScopeEntityId(updated.getScopeEntityId());
        if (updated.getCurrency() != null) existing.setCurrency(updated.getCurrency());
        existing.setHourlyRate(updated.getHourlyRate());
        existing.setDailyRate(updated.getDailyRate());
        if (updated.getEffectiveFrom() != null) existing.setEffectiveFrom(updated.getEffectiveFrom());
        existing.setEffectiveTo(updated.getEffectiveTo());
        return rateCardRepo.save(existing);
    }

    @Override
    public void delete(String id) {
        rateCardRepo.deleteById(id);
    }

    @Override
    public Optional<RateCard> resolveRate(String userId, String projectId, String roleId, LocalDate date) {
        // Precedence: USER > PROJECT > ROLE
        Optional<RateCard> userRate = rateCardRepo.findActiveForScopeOnDate(RateCard.Scope.USER, userId, date);
        if (userRate.isPresent()) return userRate;

        if (projectId != null) {
            Optional<RateCard> projectRate = rateCardRepo.findActiveForScopeOnDate(RateCard.Scope.PROJECT, projectId, date);
            if (projectRate.isPresent()) return projectRate;
        }

        if (roleId != null) {
            return rateCardRepo.findActiveForScopeOnDate(RateCard.Scope.ROLE, roleId, date);
        }

        return Optional.empty();
    }
}
