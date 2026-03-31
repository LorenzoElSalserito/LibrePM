package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.ImportMappingProfile;

import java.util.List;

public interface ImportMappingProfileService {

    List<ImportMappingProfile> listAll();
    List<ImportMappingProfile> listByEntityType(String entityType);
    ImportMappingProfile create(ImportMappingProfile profile);
    ImportMappingProfile update(String id, ImportMappingProfile profile);
    void delete(String id);
}
