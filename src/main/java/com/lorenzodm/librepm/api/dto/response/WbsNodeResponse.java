package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;
import java.util.List;

public record WbsNodeResponse(
        String id,
        String projectId,
        String parentId,
        String taskId,
        String taskTitle,
        String name,
        String wbsCode,
        int sortOrder,
        List<WbsNodeResponse> children,
        Instant createdAt
) {}
