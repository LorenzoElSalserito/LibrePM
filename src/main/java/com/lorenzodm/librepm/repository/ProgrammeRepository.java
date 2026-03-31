package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Programme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgrammeRepository extends JpaRepository<Programme, String> {
    List<Programme> findAllByOrderByNameAsc();
    List<Programme> findByStatus(String status);
}
