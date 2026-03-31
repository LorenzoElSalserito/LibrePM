package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.BudgetLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetLineRepository extends JpaRepository<BudgetLine, String> {
    List<BudgetLine> findByBudgetIdOrderBySortOrderAsc(String budgetId);
    List<BudgetLine> findByBudgetIdAndCategory(String budgetId, String category);
}
