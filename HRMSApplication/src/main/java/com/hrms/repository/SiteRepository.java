package com.hrms.repository;

import com.hrms.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {

	@Query("SELECT s FROM Site s WHERE s.id = :id AND s.isActive = true")
	Optional<Site> findByIdAndActive(@Param("id") Long id);

	Optional<Site> findByNameAndIsActiveTrue(String name);

	@Query("SELECT s FROM Site s WHERE s.isActive = true ORDER BY s.name ASC")
	List<Site> findAllActive();

	@Query("SELECT COUNT(s) FROM Site s WHERE s.isActive = true")
	long countActiveSites();
}
