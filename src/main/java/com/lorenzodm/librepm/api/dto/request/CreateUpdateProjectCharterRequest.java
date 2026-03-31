package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

public record CreateUpdateProjectCharterRequest(
        @Size(max = 255) String sponsor,
        @Size(max = 255) String projectManager,
        String objectives,
        String problemStatement,
        String businessCase
) {}
