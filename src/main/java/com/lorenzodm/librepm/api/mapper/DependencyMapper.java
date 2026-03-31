package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.DependencyResponse;
import com.lorenzodm.librepm.core.entity.Dependency;
import org.springframework.stereotype.Component;

@Component
public class DependencyMapper {

    public DependencyResponse toResponse(Dependency dependency) {
        if (dependency == null) return null;
        return new DependencyResponse(
                dependency.getId(),
                dependency.getPredecessor().getId(),
                dependency.getPredecessor().getTitle(),
                dependency.getSuccessor().getId(),
                dependency.getSuccessor().getTitle(),
                dependency.getType().name(),
                dependency.getLag(),
                dependency.getLead(),
                dependency.getCreatedAt()
        );
    }
}
