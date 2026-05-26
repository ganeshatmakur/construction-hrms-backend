package com.hrms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.dto.ApiResponse;
import com.hrms.dto.OvertimeSummaryDto;
import com.hrms.service.OvertimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

 
@Slf4j
@RestController
@RequestMapping("/api/overtime")
@RequiredArgsConstructor
public class OvertimeController {

	private final OvertimeService overtimeService;
 
	@GetMapping("/summary/{workerId}")
	public ResponseEntity<ApiResponse<OvertimeSummaryDto>> getMonthlySummary(@PathVariable Long workerId,
			@RequestParam String month) {

		log.debug("[v0] Fetching overtime summary: worker={}, month={}", workerId, month);

		OvertimeSummaryDto summary = overtimeService.getMonthlySummary(workerId, month);

		return ResponseEntity.ok(ApiResponse.success("Overtime summary retrieved for " + month, summary));
	}

 
	@PostMapping("/settle/{workerId}")
	public ResponseEntity<ApiResponse<OvertimeSummaryDto>> settleOvertime(@PathVariable Long workerId,
			@RequestParam String month) {

		log.info("[v0] Settling overtime: worker={}, month={}", workerId, month);

		OvertimeSummaryDto result = overtimeService.settleOvertimeForMonth(workerId, month);

		return ResponseEntity.ok(ApiResponse.success("Overtime settled successfully for " + month, result));
	}
}
