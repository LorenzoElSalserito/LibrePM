package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.ImportMappingProfile;
import com.lorenzodm.librepm.repository.ImportMappingProfileRepository;
import com.lorenzodm.librepm.service.ImportMappingProfileService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ImportMappingProfileServiceImpl implements ImportMappingProfileService {

    private final ImportMappingProfileRepository mappingProfileRepo;

    public ImportMappingProfileServiceImpl(ImportMappingProfileRepository mappingProfileRepo) {
        this.mappingProfileRepo = mappingProfileRepo;
    }

    @Override
    public List<ImportMappingProfile> listAll() {
        return mappingProfileRepo.findAll();
    }

    @Override
    public List<ImportMappingProfile> listByEntityType(String entityType) {
        return mappingProfileRepo.findByEntityType(entityType);
    }

    @Override
    public ImportMappingProfile create(ImportMappingProfile profile) {
        return mappingProfileRepo.save(profile);
    }

    @Override
    public ImportMappingProfile update(String id, ImportMappingProfile updated) {
        ImportMappingProfile existing = mappingProfileRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImportMappingProfile not found: " + id));
        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getEntityType() != null) existing.setEntityType(updated.getEntityType());
        if (updated.getMappingJson() != null) existing.setMappingJson(updated.getMappingJson());
        return mappingProfileRepo.save(existing);
    }

    @Override
    public void delete(String id) {
        mappingProfileRepo.deleteById(id);
    }
}
