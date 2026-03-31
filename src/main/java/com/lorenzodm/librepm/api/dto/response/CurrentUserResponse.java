package com.lorenzodm.librepm.api.dto.response;

import java.util.Map;

public record CurrentUserResponse(
        String userId,
        String name,
        Map<String, Object> attributes
) {
}
