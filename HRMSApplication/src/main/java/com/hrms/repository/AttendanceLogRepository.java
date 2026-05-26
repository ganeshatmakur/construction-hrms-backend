package com.hrms.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hrms.entity.AttendanceLog;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

	@EntityGraph(attributePaths = { "worker", "site" })
	@Query("SELECT a FROM AttendanceLog a WHERE a.worker.id = :workerId AND a.clockOutTime IS NULL")
	Optional<AttendanceLog> findActiveClockInByWorkerId(@Param("workerId") Long workerId);

	@EntityGraph(attributePaths = { "worker", "site" })
	@Query("SELECT a FROM AttendanceLog a WHERE a.worker.id = :workerId AND a.createdAt BETWEEN :from AND :to ORDER BY a.createdAt DESC")
	Page<AttendanceLog> findByWorkerIdAndDateRange(@Param("workerId") Long workerId, @Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to, Pageable pageable);

	@EntityGraph(attributePaths = { "worker", "site" })
	@Query("SELECT a FROM AttendanceLog a WHERE a.worker.id = :workerId ORDER BY a.createdAt DESC")
	Page<AttendanceLog> findByWorkerId(@Param("workerId") Long workerId, Pageable pageable);

	@EntityGraph(attributePaths = { "worker", "site" })
	@Query("SELECT a FROM AttendanceLog a WHERE a.worker.id = :workerId AND a.flagged = true ORDER BY a.createdAt DESC")
	List<AttendanceLog> findFlaggedByWorkerId(@Param("workerId") Long workerId);

	@EntityGraph(attributePaths = { "worker", "site" })
	@Query("SELECT a FROM AttendanceLog a WHERE DATE(a.clockInTime) = :date ORDER BY a.clockInTime ASC")
	List<AttendanceLog> findByDate(@Param("date") LocalDate date);

	@Query("SELECT COUNT(a) FROM AttendanceLog a WHERE a.worker.id = :workerId")
	long countByWorkerId(@Param("workerId") Long workerId);

	@EntityGraph(attributePaths = { "worker", "site" })
	@Query("SELECT a FROM AttendanceLog a WHERE a.clockOutTime IS NULL ORDER BY a.clockInTime ASC")
	List<AttendanceLog> findAllIncomplete();
}
