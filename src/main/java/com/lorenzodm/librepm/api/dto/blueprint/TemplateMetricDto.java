package com.lorenzodm.librepm.api.dto.blueprint;

public record TemplateMetricDto(
        String name,
        double targetValue,
        String unit
) {}
