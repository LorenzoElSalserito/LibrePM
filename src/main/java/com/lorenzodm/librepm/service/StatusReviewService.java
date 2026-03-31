package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.StatusReview;

import java.util.List;
import java.util.Optional;

public interface StatusReviewService {

    List<StatusReview> listByProject(String projectId);
    Optional<StatusReview> getLatest(String projectId);
    StatusReview create(StatusReview review);
    StatusReview update(String id, StatusReview review);
    void delete(String id);
}
