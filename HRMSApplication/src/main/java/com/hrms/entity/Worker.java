package com.hrms.entity;

import com.hrms.enums.WorkerDesignation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "workers", indexes = { @Index(name = "idx_worker_phone", columnList = "phone_number", unique = true),
		@Index(name = "idx_worker_active", columnList = "is_active") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Worker {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "phone_number", nullable = false, unique = true, length = 20)
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private WorkerDesignation designation;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal dailyWageRate;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal hourlyWage;

	@Column(nullable = false)
	private Boolean isActive = true;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

	public void deactivate() {
		this.isActive = false;
		this.updatedAt = LocalDateTime.now();
	}

	public boolean isActiveAndValid() {
		return this.isActive != null && this.isActive;
	}
}
