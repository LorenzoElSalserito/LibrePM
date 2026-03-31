package com.lorenzodm.librepm.api.dto.blueprint;

import java.util.List;

public record TemplateOkrDto(
        String objective,
        List<TemplateMetricDto> keyResults
) {
    public TemplateOkrDto {
        if (keyResults == null) keyResults = List.of();
    }
}
