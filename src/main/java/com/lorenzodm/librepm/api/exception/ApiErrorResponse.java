package com.lorenzodm.librepm.api.exception;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, Object> details
) {
    public static ApiErrorResponse of(int status, String error, String message, String path, Map<String, Object> details) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, details);
    }
}
