package com.hrms.dto;

import com.hrms.enums.WorkerDesignation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerDto {
	private Long id;
	private String name;
	private String phoneNumber;
	private WorkerDesignation designation;
	private BigDecimal hourlyWage;
	private Boolean isActive;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
