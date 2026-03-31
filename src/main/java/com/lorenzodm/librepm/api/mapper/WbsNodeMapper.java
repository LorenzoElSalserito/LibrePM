package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.WbsNodeResponse;
import com.lorenzodm.librepm.core.entity.WbsNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WbsNodeMapper {

    public WbsNodeResponse toResponse(WbsNode node) {
        if (node == null) return null;
        List<WbsNodeResponse> children = node.getChildren() != null
                ? node.getChildren().stream().map(this::toResponse).collect(Collectors.toList())
                : List.of();

        return new WbsNodeResponse(
                node.getId(),
                node.getProject().getId(),
                node.getParent() != null ? node.getParent().getId() : null,
                node.getTask() != null ? node.getTask().getId() : null,
                node.getTask() != null ? node.getTask().getTitle() : null,
                node.getName(),
                node.getWbsCode(),
                node.getSortOrder(),
                children,
                node.getCreatedAt()
        );
    }

    public WbsNodeResponse toResponseFlat(WbsNode node) {
        return new WbsNodeResponse(
                node.getId(),
                node.getProject().getId(),
                node.getParent() != null ? node.getParent().getId() : null,
                node.getTask() != null ? node.getTask().getId() : null,
                node.getTask() != null ? node.getTask().getTitle() : null,
                node.getName(),
                node.getWbsCode(),
                node.getSortOrder(),
                List.of(),
                node.getCreatedAt()
        );
    }
}
