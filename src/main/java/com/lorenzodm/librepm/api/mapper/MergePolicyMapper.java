package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.MergePolicyResponse;
import com.lorenzodm.librepm.core.entity.MergePolicy;
import org.springframework.stereotype.Component;

@Component
public class MergePolicyMapper {

    public MergePolicyResponse toResponse(MergePolicy mp) {
        if (mp == null) return null;
        return new MergePolicyResponse(
                mp.getId(),
                mp.getEntityType(),
                mp.getPolicy() != null ? mp.getPolicy().name() : null,
                mp.getDescription(),
                mp.isAutoResolvable(),
                mp.getFieldScope(),
                mp.getCreatedAt(),
                mp.getUpdatedAt()
        );
    }
}
