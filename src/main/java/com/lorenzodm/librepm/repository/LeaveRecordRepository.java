package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.LeaveRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRecordRepository extends JpaRepository<LeaveRecord, String> {

    List<LeaveRecord> findByUserId(String userId);

    List<LeaveRecord> findByUserIdAndLeaveDateBetween(String userId, LocalDate from, LocalDate to);

    boolean existsByUserIdAndLeaveDate(String userId, LocalDate date);
}
