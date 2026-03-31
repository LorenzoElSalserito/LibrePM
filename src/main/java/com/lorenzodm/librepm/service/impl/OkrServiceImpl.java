package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.AddSuccessMetricRequest;
import com.lorenzodm.librepm.api.dto.request.CreateOkrRequest;
import com.lorenzodm.librepm.api.dto.request.RecordAchievementRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateSuccessMetricRequest;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Okr;
import com.lorenzodm.librepm.core.entity.Project;
import com.lorenzodm.librepm.core.entity.SuccessMetric;
import com.lorenzodm.librepm.core.entity.TargetAchievedRecord;
import com.lorenzodm.librepm.repository.OkrRepository;
import com.lorenzodm.librepm.repository.ProjectRepository;
import com.lorenzodm.librepm.repository.SuccessMetricRepository;
import com.lorenzodm.librepm.repository.TargetAchievedRecordRepository;
import com.lorenzodm.librepm.service.OkrService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OkrServiceImpl implements OkrService {

    private final OkrRepository okrRepository;
    private final ProjectRepository projectRepository;
    private final SuccessMetricRepository successMetricRepository;
    private final TargetAchievedRecordRepository targetAchievedRecordRepository;

    public OkrServiceImpl(OkrRepository okrRepository,
                          ProjectRepository projectRepository,
                          SuccessMetricRepository successMetricRepository,
                          TargetAchievedRecordRepository targetAchievedRecordRepository) {
        this.okrRepository = okrRepository;
        this.projectRepository = projectRepository;
        this.successMetricRepository = successMetricRepository;
        this.targetAchievedRecordRepository = targetAchievedRecordRepository;
    }

    @Override
    public Okr create(String userId, String projectId, CreateOkrRequest req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Progetto non trovato"));
        Okr okr = new Okr();
        okr.setProject(project);
        okr.setObjective(req.objective());
        return okrRepository.save(okr);
    }

    @Override
    @Transactional(readOnly = true)
    public Okr getById(String userId, String projectId, String okrId) {
        return okrRepository.findById(okrId)
                .filter(o -> o.getProject().getId().equals(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("OKR non trovato"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Okr> listByProject(String userId, String projectId) {
        return okrRepository.findByProjectId(projectId);
    }

    @Override
    public void delete(String userId, String projectId, String okrId) {
        Okr okr = getById(userId, projectId, okrId);
        okrRepository.delete(okr);
    }

    @Override
    public SuccessMetric addKeyResult(String userId, String projectId, String okrId, AddSuccessMetricRequest req) {
        Okr okr = getById(userId, projectId, okrId);
        SuccessMetric metric = new SuccessMetric();
        metric.setOkr(okr);
        metric.setName(req.name());
        metric.setTargetValue(req.targetValue());
        metric.setCurrentValue(req.currentValue() != null ? req.currentValue() : 0.0);
        metric.setUnit(req.unit());
        return successMetricRepository.save(metric);
    }

    @Override
    public SuccessMetric updateKeyResult(String userId, String projectId, String okrId, String metricId, UpdateSuccessMetricRequest req) {
        getById(userId, projectId, okrId); // verify ownership
        SuccessMetric metric = successMetricRepository.findById(metricId)
                .orElseThrow(() -> new ResourceNotFoundException("Metrica non trovata"));
        if (req.name() != null) metric.setName(req.name());
        if (req.targetValue() != null) metric.setTargetValue(req.targetValue());
        if (req.currentValue() != null) metric.setCurrentValue(req.currentValue());
        if (req.unit() != null) metric.setUnit(req.unit());
        return successMetricRepository.save(metric);
    }

    @Override
    public void removeKeyResult(String userId, String projectId, String okrId, String metricId) {
        getById(userId, projectId, okrId); // verify ownership
        SuccessMetric metric = successMetricRepository.findById(metricId)
                .orElseThrow(() -> new ResourceNotFoundException("Metrica non trovata"));
        successMetricRepository.delete(metric);
    }

    @Override
    public TargetAchievedRecord recordAchievement(String userId, String projectId, String okrId, String metricId, RecordAchievementRequest req) {
        getById(userId, projectId, okrId); // verify ownership
        SuccessMetric metric = successMetricRepository.findById(metricId)
                .orElseThrow(() -> new ResourceNotFoundException("Metrica non trovata"));

        TargetAchievedRecord record = new TargetAchievedRecord();
        record.setMetric(metric);
        record.setAchievedValue(req.achievedValue());
        record.setRecordDate(req.recordDate());
        record.setNote(req.note());

        // Update current value on metric
        metric.setCurrentValue(req.achievedValue());
        successMetricRepository.save(metric);

        return targetAchievedRecordRepository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TargetAchievedRecord> getAchievementHistory(String userId, String projectId, String okrId, String metricId) {
        getById(userId, projectId, okrId); // verify ownership
        return targetAchievedRecordRepository.findByMetricIdOrderByRecordDateDesc(metricId);
    }
}
