package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Okr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OkrRepository extends JpaRepository<Okr, String> {

    List<Okr> findByProjectId(String projectId);
}
