package com.hrms.repository;

import com.hrms.entity.OvertimeEntry;
import com.hrms.enums.SettlementStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

 
public interface OvertimeEntryRepository extends JpaRepository<OvertimeEntry, Long> {

  
    @EntityGraph(attributePaths = {"worker", "attendanceLog"})
    @Query("SELECT o FROM OvertimeEntry o WHERE o.worker.id = :workerId AND o.yearMonth = :yearMonth ORDER BY o.overtimeDate ASC")
    List<OvertimeEntry> findByWorkerIdAndYearMonth(
        @Param("workerId") Long workerId,
        @Param("yearMonth") String yearMonth
    );

   
    @EntityGraph(attributePaths = {"worker", "attendanceLog"})
    @Query("SELECT o FROM OvertimeEntry o WHERE o.worker.id = :workerId AND o.yearMonth = :yearMonth AND o.settlementStatus = 'PENDING' ORDER BY o.overtimeDate ASC")
    List<OvertimeEntry> findPendingByWorkerIdAndYearMonth(
        @Param("workerId") Long workerId,
        @Param("yearMonth") String yearMonth
    );

    
    @Query("SELECT COALESCE(SUM(o.overtimeHours), 0) FROM OvertimeEntry o WHERE o.worker.id = :workerId AND o.yearMonth = :yearMonth")
    BigDecimal sumOvertimeHoursByWorkerAndMonth(
        @Param("workerId") Long workerId,
        @Param("yearMonth") String yearMonth
    );

    
    @Query("SELECT COALESCE(SUM(o.overtimeAmount), 0) FROM OvertimeEntry o WHERE o.worker.id = :workerId AND o.yearMonth = :yearMonth")
    BigDecimal sumOvertimeAmountByWorkerAndMonth(
        @Param("workerId") Long workerId,
        @Param("yearMonth") String yearMonth
    );

    
    @EntityGraph(attributePaths = {"worker"})
    @Query("SELECT o FROM OvertimeEntry o WHERE o.worker.id = :workerId AND o.yearMonth = :yearMonth AND o.settlementStatus = 'PENDING'")
    List<OvertimeEntry> findAllPendingForSettlement(
        @Param("workerId") Long workerId,
        @Param("yearMonth") String yearMonth
    );

    
    @Modifying
    @Query("UPDATE OvertimeEntry o SET o.settlementStatus = :newStatus, o.updatedAt = CURRENT_TIMESTAMP WHERE o.worker.id = :workerId AND o.yearMonth = :yearMonth AND o.settlementStatus = 'PENDING'")
    int updateSettlementStatusForMonth(
        @Param("workerId") Long workerId,
        @Param("yearMonth") String yearMonth,
        @Param("newStatus") SettlementStatus newStatus
    );

   
    @Query("SELECT COUNT(o) FROM OvertimeEntry o WHERE o.worker.id = :workerId AND o.settlementStatus = 'PENDING'")
    long countPendingByWorkerId(@Param("workerId") Long workerId);

    
    @EntityGraph(attributePaths = {"worker", "attendanceLog"})
    @Query("SELECT o FROM OvertimeEntry o WHERE o.overtimeDate = :date ORDER BY o.worker.id ASC")
    List<OvertimeEntry> findByDate(@Param("date") LocalDate date);

    
    @Query("SELECT COALESCE(SUM(o.overtimeHours), 0) >= :monthlyCapHours FROM OvertimeEntry o WHERE o.worker.id = :workerId AND o.yearMonth = :yearMonth")
    boolean isMonthlyCapExceeded(
        @Param("workerId") Long workerId,
        @Param("yearMonth") String yearMonth,
        @Param("monthlyCapHours") Double monthlyCapHours
    );

     
    @Query("SELECT :monthlyCapHours - COALESCE(SUM(o.overtimeHours), 0) FROM OvertimeEntry o WHERE o.worker.id = :workerId AND o.yearMonth = :yearMonth")
    BigDecimal getRemainingOvertimeCapacity(
        @Param("workerId") Long workerId,
        @Param("yearMonth") String yearMonth,
        @Param("monthlyCapHours") Double monthlyCapHours
    );
}
