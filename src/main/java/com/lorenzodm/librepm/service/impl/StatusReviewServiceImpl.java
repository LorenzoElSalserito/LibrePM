package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.StatusReview;
import com.lorenzodm.librepm.repository.StatusReviewRepository;
import com.lorenzodm.librepm.service.StatusReviewService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StatusReviewServiceImpl implements StatusReviewService {

    private final StatusReviewRepository statusReviewRepo;

    public StatusReviewServiceImpl(StatusReviewRepository statusReviewRepo) {
        this.statusReviewRepo = statusReviewRepo;
    }

    @Override
    public List<StatusReview> listByProject(String projectId) {
        return statusReviewRepo.findByProjectIdOrderByReviewDateDesc(projectId);
    }

    @Override
    public Optional<StatusReview> getLatest(String projectId) {
        return statusReviewRepo.findLatestByProjectId(projectId);
    }

    @Override
    public StatusReview create(StatusReview review) {
        return statusReviewRepo.save(review);
    }

    @Override
    public StatusReview update(String id, StatusReview updated) {
        StatusReview existing = statusReviewRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StatusReview not found: " + id));
        if (updated.getOverallStatus() != null) existing.setOverallStatus(updated.getOverallStatus());
        if (updated.getScheduleStatus() != null) existing.setScheduleStatus(updated.getScheduleStatus());
        if (updated.getBudgetStatus() != null) existing.setBudgetStatus(updated.getBudgetStatus());
        if (updated.getRiskStatus() != null) existing.setRiskStatus(updated.getRiskStatus());
        if (updated.getSummary() != null) existing.setSummary(updated.getSummary());
        if (updated.getAchievements() != null) existing.setAchievements(updated.getAchievements());
        if (updated.getBlockers() != null) existing.setBlockers(updated.getBlockers());
        if (updated.getNextActions() != null) existing.setNextActions(updated.getNextActions());
        return statusReviewRepo.save(existing);
    }

    @Override
    public void delete(String id) {
        statusReviewRepo.deleteById(id);
    }
}
