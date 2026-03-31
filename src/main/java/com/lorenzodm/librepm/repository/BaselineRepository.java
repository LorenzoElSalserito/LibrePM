package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Baseline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaselineRepository extends JpaRepository<Baseline, String> {

    List<Baseline> findByProjectIdOrderBySnapshotDateDesc(String projectId);
}
