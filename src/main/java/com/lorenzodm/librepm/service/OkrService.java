package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.AddSuccessMetricRequest;
import com.lorenzodm.librepm.api.dto.request.CreateOkrRequest;
import com.lorenzodm.librepm.api.dto.request.RecordAchievementRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateSuccessMetricRequest;
import com.lorenzodm.librepm.core.entity.Okr;
import com.lorenzodm.librepm.core.entity.SuccessMetric;
import com.lorenzodm.librepm.core.entity.TargetAchievedRecord;

import java.util.List;

public interface OkrService {

    Okr create(String userId, String projectId, CreateOkrRequest req);

    Okr getById(String userId, String projectId, String okrId);

    List<Okr> listByProject(String userId, String projectId);

    void delete(String userId, String projectId, String okrId);

    SuccessMetric addKeyResult(String userId, String projectId, String okrId, AddSuccessMetricRequest req);

    SuccessMetric updateKeyResult(String userId, String projectId, String okrId, String metricId, UpdateSuccessMetricRequest req);

    void removeKeyResult(String userId, String projectId, String okrId, String metricId);

    TargetAchievedRecord recordAchievement(String userId, String projectId, String okrId, String metricId, RecordAchievementRequest req);

    List<TargetAchievedRecord> getAchievementHistory(String userId, String projectId, String okrId, String metricId);
}
