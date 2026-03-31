package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.ActualCostRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActualCostRecordRepository extends JpaRepository<ActualCostRecord, String> {
    List<ActualCostRecord> findByBudgetLineIdOrderByCostDateDesc(String budgetLineId);
}
