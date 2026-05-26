package com.hrms.service.impl;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.hrms.dto.OvertimeSummaryDto;
import com.hrms.entity.AttendanceLog;
import com.hrms.entity.OvertimeEntry;
import com.hrms.entity.Worker;
import com.hrms.enums.SettlementStatus;
import com.hrms.event.SettlementCompletedEvent;
import com.hrms.exception.ApiException;
import com.hrms.exception.ErrorCode;
import com.hrms.repository.OvertimeEntryRepository;
import com.hrms.service.OvertimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OvertimeServiceImpl implements OvertimeService {

	private final OvertimeEntryRepository overtimeEntryRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Value("${app.overtime.daily-standard-hours:8}")
	private int standardHours;

	@Value("${app.overtime.monthly-cap-hours:60}")
	private int monthlyCapHours;

	@Value("${app.overtime.rate-15x-multiplier:1.5}")
	private double rate15xMultiplier;

	@Value("${app.overtime.rate-2x-multiplier:2.0}")
	private double rate2xMultiplier;

	@Override
	public void createOvertimeEntry(AttendanceLog attendanceLog, Worker worker) {

		log.info("[v0] Creating overtime entry: attendance_id={}, worker={}, overtime_hours={}", attendanceLog.getId(),
				worker.getId(), attendanceLog.getOvertimeHours());

		BigDecimal overtimeHours = attendanceLog.getOvertimeHours();
		if (overtimeHours == null || overtimeHours.compareTo(BigDecimal.ZERO) <= 0) {
			log.debug("[v0] No overtime to record");
			return;
		}

		// Check monthly cap
		String yearMonth = attendanceLog.getAttendanceDate()
				.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
		BigDecimal currentMonthOvertime = overtimeEntryRepository.sumOvertimeHoursByWorkerAndMonth(worker.getId(),
				yearMonth);

		if (currentMonthOvertime == null) {
			currentMonthOvertime = BigDecimal.ZERO;
		}

		BigDecimal projectedTotal = currentMonthOvertime.add(overtimeHours);
		if (projectedTotal.compareTo(BigDecimal.valueOf(monthlyCapHours)) > 0) {
			log.warn("[v0] Monthly overtime cap would be exceeded: worker={}, current={}, new={}, cap={}",
					worker.getId(), currentMonthOvertime, overtimeHours, monthlyCapHours);
			// Reduce to cap
			overtimeHours = BigDecimal.valueOf(monthlyCapHours).subtract(currentMonthOvertime);
			if (overtimeHours.compareTo(BigDecimal.ZERO) <= 0) {
				log.info("[v0] Worker has reached monthly cap, no additional overtime recorded");
				return;
			}
		}

		// Calculate multiplier based on hours
		BigDecimal multiplier = calculateOvertimeMultiplier(overtimeHours);

		// Create overtime entry
		OvertimeEntry entry = OvertimeEntry.builder().worker(worker).attendanceLog(attendanceLog)
				.overtimeDate(attendanceLog.getAttendanceDate()).yearMonth(yearMonth).overtimeHours(overtimeHours)
				.multiplier(multiplier).build();

		// Calculate amount
		entry.calculateOvertimeAmount(worker.getHourlyWage());

		entry = overtimeEntryRepository.save(entry);
		log.info("[v0] Overtime entry created: id={}, amount={}, multiplier={}", entry.getId(),
				entry.getOvertimeAmount(), multiplier);

		// TODO Auto-generated method stub

	}

	@Override
	public OvertimeSummaryDto getMonthlySummary(Long workerId, String yearMonth) {

		log.debug("[v0] Fetching overtime summary: worker={}, month={}", workerId, yearMonth);

		// Validate year-month format
		YearMonth.parse(yearMonth);

		// Fetch all overtime entries for the month
		List<OvertimeEntry> entries = overtimeEntryRepository.findByWorkerIdAndYearMonth(workerId, yearMonth);

		if (entries.isEmpty()) {
			log.debug("[v0] No overtime entries found for worker={}, month={}", workerId, yearMonth);
			return buildEmptySummary(workerId, yearMonth);
		}

		Worker worker = entries.get(0).getWorker();

		BigDecimal totalHours = entries.stream().map(OvertimeEntry::getOvertimeHours).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		BigDecimal totalAmount = entries.stream().map(OvertimeEntry::getOvertimeAmount).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		// Determine settlement status (all entries should have same status)
		SettlementStatus status = entries.stream().map(OvertimeEntry::getSettlementStatus).distinct().findFirst()
				.orElse(SettlementStatus.PENDING);

		List<OvertimeSummaryDto.OvertimeDailyBreakdownDto> breakdown = entries.stream()
				.map(entry -> OvertimeSummaryDto.OvertimeDailyBreakdownDto.builder().date(entry.getOvertimeDate())
						.overtimeHours(entry.getOvertimeHours()).multiplier(entry.getMultiplier())
						.payableAmount(entry.getOvertimeAmount()).status(entry.getSettlementStatus()).build())
				.collect(Collectors.toList());

		return OvertimeSummaryDto.builder().workerId(workerId).workerName(worker.getName()).yearMonth(yearMonth)
				.totalOvertimeHours(totalHours).totalPayableAmount(totalAmount).settlementStatus(status)
				.dailyBreakdown(breakdown).build();

	}

	@Override
	public OvertimeSummaryDto settleOvertimeForMonth(Long workerId, String yearMonth) {

		log.info("[v0] Settlement attempt: worker={}, month={}", workerId, yearMonth);

		// Validate month format
		YearMonth entryMonth = YearMonth.parse(yearMonth);
		YearMonth currentMonth = YearMonth.now();

		// Cannot settle current month or future months
		if (!entryMonth.isBefore(currentMonth)) {
			throw new ApiException(ErrorCode.CANNOT_SETTLE_CURRENT_MONTH,
					"Cannot settle overtime for current or future months");
		}

		// Fetch all pending entries for settlement
		List<OvertimeEntry> pendingEntries = overtimeEntryRepository.findAllPendingForSettlement(workerId, yearMonth);

		if (pendingEntries.isEmpty()) {
			log.warn("[v0] No pending entries to settle: worker={}, month={}", workerId, yearMonth);
			return buildEmptySummary(workerId, yearMonth);
		}

		// Calculate total payable amount BEFORE updating
		BigDecimal totalAmount = pendingEntries.stream().map(OvertimeEntry::getOvertimeAmount).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		// Mark all entries as SETTLED in atomic operation
		for (OvertimeEntry entry : pendingEntries) {
			entry.markAsSettled();
		}
		overtimeEntryRepository.saveAll(pendingEntries);

		log.info("[v0] Settlement completed: worker={}, month={}, amount={}, entries={}", workerId, yearMonth,
				totalAmount, pendingEntries.size());

		// Publish event for post-commit actions (SMS notification)
		Worker worker = pendingEntries.get(0).getWorker();
		SettlementCompletedEvent event = new SettlementCompletedEvent(workerId, worker.getName(),
				worker.getPhoneNumber(), yearMonth, totalAmount, pendingEntries.size());
		eventPublisher.publishEvent(event);

		return getMonthlySummary(workerId, yearMonth);

	}

	private OvertimeSummaryDto buildEmptySummary(Long workerId, String yearMonth) {
		return OvertimeSummaryDto.builder().workerId(workerId).yearMonth(yearMonth).totalOvertimeHours(BigDecimal.ZERO)
				.totalPayableAmount(BigDecimal.ZERO).settlementStatus(SettlementStatus.PENDING)
				.dailyBreakdown(List.of()).build();
	}

	private BigDecimal calculateOvertimeMultiplier(BigDecimal overtimeHours) {
		if (overtimeHours == null) {
			return BigDecimal.valueOf(rate15xMultiplier);
		}

		// If overtime <= 2 hours, use 1.5x rate
		if (overtimeHours.compareTo(BigDecimal.valueOf(2)) <= 0) {
			return BigDecimal.valueOf(rate15xMultiplier);
		}

		// If overtime > 2 hours, use weighted average of 1.5x and 2x
		// This is simplified; actual calculation happens per-entry
		return BigDecimal.valueOf(rate2xMultiplier);
	}

}
