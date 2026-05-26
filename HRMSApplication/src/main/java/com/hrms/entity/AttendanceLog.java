package com.hrms.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attendance_logs", indexes = {
		@Index(name = "idx_attendance_worker_date", columnList = "worker_id,created_at"),
		@Index(name = "idx_attendance_active", columnList = "clock_out_time"),
		@Index(name = "idx_attendance_date_range", columnList = "created_at") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "worker_id", nullable = false)
	private Worker worker;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_id", nullable = false)
	private Site site;

	@Column(nullable = false)
	private LocalDateTime clockInTime;

	@Column
	private LocalDateTime clockOutTime;

	@Column
	private BigDecimal totalHours;

	@Column
	private BigDecimal overtimeHours;

	@Column
	private Boolean flagged = false;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

 
	public BigDecimal calculateTotalHours() {
		if (this.clockOutTime == null) {
			return null;
		}
		long minutes = ChronoUnit.MINUTES.between(this.clockInTime, this.clockOutTime);
		double hours = minutes / 60.0;
		this.totalHours = BigDecimal.valueOf(hours);
		return this.totalHours;
	}

	public BigDecimal calculateOvertimeHours() {
		if (this.totalHours == null) {
			return null;
		}
		BigDecimal standardHours = BigDecimal.valueOf(8);
		BigDecimal overtime = this.totalHours.subtract(standardHours);

		if (overtime.compareTo(BigDecimal.ZERO) < 0) {
			this.overtimeHours = BigDecimal.ZERO;
		} else {
			this.overtimeHours = overtime;
		}
		return this.overtimeHours;
	}

	 
	public void flagLongShiftIfNeeded() {
		if (this.totalHours != null && this.totalHours.compareTo(BigDecimal.valueOf(16)) > 0) {
			this.flagged = true;
		}
	}

	public void completeAttendance(LocalDateTime clockOutTime) {
		this.clockOutTime = clockOutTime;
		this.updatedAt = LocalDateTime.now();
		this.calculateTotalHours();
		this.calculateOvertimeHours();
		this.flagLongShiftIfNeeded();
	}

	public boolean isActiveClockin() {
		return this.clockOutTime == null;
	}

	public LocalDate getAttendanceDate() {
		return this.clockInTime != null ? this.clockInTime.toLocalDate() : null;
	}
}
