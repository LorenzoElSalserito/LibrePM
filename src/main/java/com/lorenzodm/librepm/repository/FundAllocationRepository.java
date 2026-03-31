package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.FundAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundAllocationRepository extends JpaRepository<FundAllocation, String> {
    List<FundAllocation> findByFundingSourceId(String fundingSourceId);
    List<FundAllocation> findByBudgetLineId(String budgetLineId);
}
