package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT r FROM Role r WHERE r.deletedAt IS NULL ORDER BY r.name ASC")
    List<Role> findAllActive();
}
