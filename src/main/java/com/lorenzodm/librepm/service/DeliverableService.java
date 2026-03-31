package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateDeliverableRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateDeliverableRequest;
import com.lorenzodm.librepm.core.entity.Deliverable;

import java.util.List;

public interface DeliverableService {

    Deliverable create(String userId, String projectId, CreateDeliverableRequest req);

    Deliverable getById(String userId, String projectId, String deliverableId);

    List<Deliverable> listByProject(String userId, String projectId);

    Deliverable update(String userId, String projectId, String deliverableId, UpdateDeliverableRequest req);

    void delete(String userId, String projectId, String deliverableId);
}
