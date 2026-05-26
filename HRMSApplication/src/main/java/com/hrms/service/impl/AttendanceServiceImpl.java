package com.hrms.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hrms.dto.AttendanceLogDto;
import com.hrms.dto.WorkerDto;
import com.hrms.entity.AttendanceLog;
import com.hrms.entity.Site;
import com.hrms.entity.Worker;
import com.hrms.exception.ApiException;
import com.hrms.exception.ErrorCode;
import com.hrms.repository.AttendanceLogRepository;
import com.hrms.repository.OvertimeEntryRepository;
import com.hrms.repository.SiteRepository;
import com.hrms.repository.WorkerRepository;
import com.hrms.service.AttendanceService;
import com.hrms.service.CacheService;
import com.hrms.service.OvertimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {
	

    private final AttendanceLogRepository attendanceLogRepository;
    private final WorkerRepository workerRepository;
    private final SiteRepository siteRepository;
    private final CacheService cacheService;
    private final OvertimeService overtimeService;


	@Override
	@Transactional
	public AttendanceLogDto clockIn(Long workerId, Long siteId) {

        log.info("[v0] Clock-in attempt for worker={}, site={}", workerId, siteId);

        // Validate worker exists and is active
        Worker worker = workerRepository.findByIdAndActive(workerId)
            .orElseThrow(() -> new ApiException(ErrorCode.WORKER_NOT_FOUND, "Worker not found or inactive"));

        // Validate site exists and is active
        Site site = siteRepository.findByIdAndActive(siteId)
            .orElseThrow(() -> new ApiException(ErrorCode.SITE_NOT_FOUND, "Site not found or inactive"));

        // Check if worker is already clocked in (prevent double clock-in)
        boolean alreadyClockedIn = attendanceLogRepository
            .findActiveClockInByWorkerId(workerId)
            .isPresent();

        if (alreadyClockedIn) {
            throw new ApiException(ErrorCode.ALREADY_CLOCKED_IN,
                "Worker is already clocked in at another site. Please clock out first.");
        }

        // Validate clock-in time is not in the future
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.INVALID_TIME, "Cannot clock in with future timestamp");
        }

        // Create and save attendance log
        AttendanceLog attendanceLog = AttendanceLog.builder()
            .worker(worker)
            .site(site)
            .clockInTime(now)
            .build();

        attendanceLog = attendanceLogRepository.save(attendanceLog);
        log.info("[v0] Clock-in successful: attendance_id={}, worker={}", attendanceLog.getId(), workerId);

        // Cache active worker in Redis (16-hour TTL)
        try {
            cacheService.addActiveWorker(workerId, worker.getName(), site.getName(), now);
        } catch (Exception e) {
            log.warn("[v0] Failed to cache active worker in Redis, continuing without cache: {}", e.getMessage());
        }

        return mapToDto(attendanceLog);
    
	}

	@Override
	public AttendanceLogDto clockOut(Long workerId) {

        log.info("[v0] Clock-out attempt for worker={}", workerId);

        // Find active clock-in record
        AttendanceLog attendanceLog = attendanceLogRepository
            .findActiveClockInByWorkerId(workerId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_CLOCKED_IN,
                "Worker is not currently clocked in"));

        Worker worker = attendanceLog.getWorker();
        LocalDateTime now = LocalDateTime.now();

        // Complete attendance log with clock-out time
        attendanceLog.completeAttendance(now);
        attendanceLog = attendanceLogRepository.save(attendanceLog);

        log.info("[v0] Clock-out successful: attendance_id={}, total_hours={}, overtime_hours={}, flagged={}",
            attendanceLog.getId(), attendanceLog.getTotalHours(), attendanceLog.getOvertimeHours(), attendanceLog.getFlagged());

        // Create overtime entry if overtime hours > 0
        if (attendanceLog.getOvertimeHours() != null && attendanceLog.getOvertimeHours().compareTo(BigDecimal.ZERO) > 0) {
            overtimeService.createOvertimeEntry(attendanceLog, worker);
        }

        // Remove worker from active cache
        try {
            cacheService.removeActiveWorker(workerId);
        } catch (Exception e) {
            log.warn("[v0] Failed to remove worker from cache: {}", e.getMessage());
        }

        return mapToDto(attendanceLog);
    
	}

	@Override
	public List<WorkerDto> getActiveWorkers() {

        log.debug("[v0] Fetching active workers from cache");
        try {
            return cacheService.getActiveWorkers();
        } catch (Exception e) {
            log.error("[v0] Failed to fetch active workers from Redis: {}", e.getMessage());
            // Return empty list on Redis failure (graceful degradation)
            return List.of();
        }
    
	}

	@Override
	@Transactional(readOnly = true) 
	public Page<AttendanceLogDto> getAttendanceHistory(Long workerId, int page, int size) {

        log.debug("[v0] Fetching attendance history: worker={}, page={}, size={}", workerId, page, size);

        // Validate worker exists
        if (!workerRepository.existsById(workerId)) {
            throw new ApiException(ErrorCode.WORKER_NOT_FOUND, "Worker not found");
        }

        // Limit page size to prevent abuse
        if (size > 100) {
            size = 100;
        }
        if (size < 1) {
            size = 20;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceLog> logs = attendanceLogRepository.findByWorkerId(workerId, pageable);

        return logs.map(this::mapToDto);
    
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AttendanceLogDto> getAttendanceByDateRange(Long workerId, LocalDateTime from, LocalDateTime to,
			int page, int size) {

        
        log.debug("[v0] Fetching attendance range: worker={}, from={}, to={}", workerId, from, to);

        if (!workerRepository.existsById(workerId)) {
            throw new ApiException(ErrorCode.WORKER_NOT_FOUND, "Worker not found");
        }

        if (size > 100) size = 100;
        if (size < 1) size = 20;

        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceLog> logs = attendanceLogRepository
            .findByWorkerIdAndDateRange(workerId, from, to, pageable);

        return logs.map(this::mapToDto);
    
	}

	@Override
	public List<AttendanceLogDto> getFlaggedAttendance(Long workerId) {

        log.debug("[v0] Fetching flagged attendance for worker={}", workerId);

        if (!workerRepository.existsById(workerId)) {
            throw new ApiException(ErrorCode.WORKER_NOT_FOUND, "Worker not found");
        }

        return attendanceLogRepository.findFlaggedByWorkerId(workerId)
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    
	}
	
	 private AttendanceLogDto mapToDto(AttendanceLog entity) {
	        return AttendanceLogDto.builder()
	            .id(entity.getId())
	            .worker(mapWorkerToDto(entity.getWorker()))
	            .clockInTime(entity.getClockInTime())
	            .clockOutTime(entity.getClockOutTime())
	            .totalHours(entity.getTotalHours())
	            .overtimeHours(entity.getOvertimeHours())
	            .flagged(entity.getFlagged())
	            .createdAt(entity.getCreatedAt())
	            .build();
	    }

	    private WorkerDto mapWorkerToDto(Worker worker) {
	        return WorkerDto.builder()
	            .id(worker.getId())
	            .name(worker.getName())
	            .phoneNumber(worker.getPhoneNumber())
	            .designation(worker.getDesignation())
	            .hourlyWage(worker.getHourlyWage())
	            .build();
	    }

}
