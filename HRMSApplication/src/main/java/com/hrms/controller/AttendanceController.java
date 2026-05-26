package com.hrms.controller;

import com.hrms.dto.ApiResponse;
import com.hrms.dto.AttendanceLogDto;
import com.hrms.dto.ClockInRequest;
import com.hrms.dto.ClockOutRequest;
import com.hrms.dto.WorkerDto;
import com.hrms.service.AttendanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

 
@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

     
    @PostMapping("/clock-in")
    public ResponseEntity<ApiResponse<AttendanceLogDto>> clockIn(
        @Valid @RequestBody ClockInRequest request) {
        
        log.info("[v0] Clock-in request: worker={}, site={}", request.getWorkerId(), request.getSiteId());
        
        AttendanceLogDto result = attendanceService.clockIn(request.getWorkerId(), request.getSiteId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Clock in successful", result));
    }

     
    @PostMapping("/clock-out")
    public ResponseEntity<ApiResponse<AttendanceLogDto>> clockOut(
        @Valid @RequestBody ClockOutRequest request) {
        
        log.info("[v0] Clock-out request: worker={}", request.getWorkerId());
        
        AttendanceLogDto result = attendanceService.clockOut(request.getWorkerId());
        
        return ResponseEntity.ok(ApiResponse.success("Clock out successful", result));
    }

   
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<WorkerDto>>> getActiveWorkers() {
        log.debug("[v0] Fetching active workers");
        
        List<WorkerDto> activeWorkers = attendanceService.getActiveWorkers();
        
        return ResponseEntity.ok(ApiResponse.success(
            "Active workers retrieved: " + activeWorkers.size(),
            activeWorkers
        ));
    }

    
    @GetMapping("/log")
    public ResponseEntity<ApiResponse<Page<AttendanceLogDto>>> getAttendanceHistory(
        @RequestParam Long workerId,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        log.debug("[v0] Fetching attendance history: worker={}, page={}, size={}", workerId, page, size);
        
        Page<AttendanceLogDto> result = attendanceService.getAttendanceHistory(workerId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success("Attendance history retrieved", result));
    }


}
