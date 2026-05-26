package com.hrms.dto;

import com.hrms.enums.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeSummaryDto {
	private Long workerId;
	private String workerName;
	private String yearMonth;
	private BigDecimal totalOvertimeHours;
	private BigDecimal totalPayableAmount;
	private SettlementStatus settlementStatus;
	private List<OvertimeDailyBreakdownDto> dailyBreakdown;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class OvertimeDailyBreakdownDto {
		private LocalDate date;
		private BigDecimal overtimeHours;
		private BigDecimal multiplier; // 1.5 or 2.0
		private BigDecimal payableAmount;
		private SettlementStatus status;
	}
}
