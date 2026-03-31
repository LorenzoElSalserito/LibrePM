package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.FinancialBaseline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialBaselineRepository extends JpaRepository<FinancialBaseline, String> {
    List<FinancialBaseline> findByBudgetIdOrderByCreatedAtDesc(String budgetId);
}
