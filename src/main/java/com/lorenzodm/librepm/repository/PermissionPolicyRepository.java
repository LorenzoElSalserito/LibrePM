package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.PermissionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionPolicyRepository extends JpaRepository<PermissionPolicy, String> {

    List<PermissionPolicy> findByRoleId(String roleId);

    @Query("SELECT pp FROM PermissionPolicy pp WHERE pp.role.id = :roleId AND pp.permission = :permission AND pp.deletedAt IS NULL")
    java.util.Optional<PermissionPolicy> findByRoleIdAndPermission(@Param("roleId") String roleId, @Param("permission") String permission);

    boolean existsByRoleIdAndPermission(String roleId, String permission);

    @Query("SELECT pp.permission FROM PermissionPolicy pp WHERE pp.role.id = :roleId AND pp.deletedAt IS NULL")
    List<String> findPermissionsByRoleId(@Param("roleId") String roleId);

    void deleteByRoleId(String roleId);
}
