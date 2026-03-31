package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.DataCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataCategoryRepository extends JpaRepository<DataCategory, String> {
    List<DataCategory> findAllByOrderByNameAsc();
}
