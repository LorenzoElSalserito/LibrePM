package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.RoleResponse;
import com.lorenzodm.librepm.core.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleMapper {

    public RoleResponse toResponse(Role role, List<String> permissions) {
        if (role == null) return null;
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                permissions,
                role.getCreatedAt()
        );
    }
}
