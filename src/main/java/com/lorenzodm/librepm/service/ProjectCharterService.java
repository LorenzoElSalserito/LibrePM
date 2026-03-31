package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateUpdateProjectCharterRequest;
import com.lorenzodm.librepm.core.entity.ProjectCharter;

public interface ProjectCharterService {

    ProjectCharter upsert(String userId, String projectId, CreateUpdateProjectCharterRequest req);

    ProjectCharter getByProjectId(String userId, String projectId);
}
