package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateExternalContributorRequest;
import com.lorenzodm.librepm.core.entity.ExternalContributor;

import java.util.List;

public interface ExternalContributorService {
    ExternalContributor create(String userId, CreateExternalContributorRequest req);
    List<ExternalContributor> listByScope(String scopeEntityId);
    List<ExternalContributor> listAll();
    void revoke(String userId, String contributorId);
}
