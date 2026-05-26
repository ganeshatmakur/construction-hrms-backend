package com.hrms.entity;

import com.hrms.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "overtime_entries", indexes = {
		@Index(name = "idx_overtime_worker_month", columnList = "worker_id,year_month"),
		@Index(name = "idx_overtime_status", columnList = "settlement_status"),
		@Index(name = "idx_overtime_date", columnList = "overtime_date") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "worker_id", nullable = false)
	private Worker worker;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "attendance_log_id", nullable = false)
	private AttendanceLog attendanceLog;

	@Column(nullable = false)
	private LocalDate overtimeDate;

	@Column(nullable = false, length = 7) // Format: YYYY-MM
	private String yearMonth;

	@Column(nullable = false, precision = 8, scale = 2)
	private BigDecimal overtimeHours;

	@Column(nullable = false)
	private BigDecimal multiplier; // 1.5 or 2.0

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal overtimeAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SettlementStatus settlementStatus = SettlementStatus.PENDING;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

	public BigDecimal calculateOvertimeAmount(BigDecimal hourlyWage) {
		if (this.overtimeHours == null || this.multiplier == null || hourlyWage == null) {
			return BigDecimal.ZERO;
		}
		this.overtimeAmount = this.overtimeHours.multiply(this.multiplier).multiply(hourlyWage).setScale(2,
				java.math.RoundingMode.HALF_UP);
		return this.overtimeAmount;
	}

	public void markAsSettled() {
		this.settlementStatus = SettlementStatus.SETTLED;
		this.updatedAt = LocalDateTime.now();
	}

	public boolean isPending() {
		return this.settlementStatus == SettlementStatus.PENDING;
	}

	public boolean isSettled() {
		return this.settlementStatus == SettlementStatus.SETTLED;
	}

	public boolean isEligibleForSettlement() {
		YearMonth entryMonth = YearMonth.parse(this.yearMonth);
		YearMonth currentMonth = YearMonth.now();
		return entryMonth.isBefore(currentMonth) && this.isPending();
	}

	public YearMonth getYearMonthObject() {
		return YearMonth.parse(this.yearMonth);
	}
}
