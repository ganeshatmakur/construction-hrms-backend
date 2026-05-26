package com.hrms.repository;

import com.hrms.entity.Worker;
import com.hrms.enums.WorkerDesignation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

	@Query("SELECT w FROM Worker w WHERE w.id = :id AND w.isActive = true")
	Optional<Worker> findByIdAndActive(@Param("id") Long id);

	Optional<Worker> findByPhoneNumber(String phoneNumber);

	@Query("SELECT w FROM Worker w WHERE w.isActive = true AND w.designation = :designation")
	List<Worker> findByDesignationAndActive(@Param("designation") WorkerDesignation designation);

	@Query("SELECT w FROM Worker w WHERE w.isActive = true ORDER BY w.name ASC")
	List<Worker> findAllActive();

	@Query("SELECT COUNT(w) FROM Worker w WHERE w.isActive = true")
	long countActiveWorkers();
}
