package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.WbsNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WbsNodeRepository extends JpaRepository<WbsNode, String> {

    List<WbsNode> findByProjectIdOrderBySortOrderAsc(String projectId);

    List<WbsNode> findByProjectIdAndParentIsNullOrderBySortOrderAsc(String projectId);

    List<WbsNode> findByParentIdOrderBySortOrderAsc(String parentId);

    Optional<WbsNode> findByTaskId(String taskId);

    boolean existsByProjectIdAndWbsCode(String projectId, String wbsCode);
}
