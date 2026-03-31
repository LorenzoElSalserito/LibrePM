package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.ImportExportJobResponse;
import com.lorenzodm.librepm.core.entity.ImportExportJob;
import org.springframework.stereotype.Component;

@Component
public class ImportExportJobMapper {

    public ImportExportJobResponse toResponse(ImportExportJob job) {
        if (job == null) return null;
        return new ImportExportJobResponse(
                job.getId(),
                job.getUserId(),
                job.getProjectId(),
                job.getJobType() != null ? job.getJobType().name() : null,
                job.getStatus() != null ? job.getStatus().name() : null,
                job.getResultPayload(),
                job.getErrorMessage(),
                job.getRecordCount(),
                job.getCompletedAt(),
                job.getCreatedAt()
        );
    }
}
