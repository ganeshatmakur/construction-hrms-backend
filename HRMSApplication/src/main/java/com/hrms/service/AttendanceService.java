package com.hrms.service;

import com.hrms.dto.AttendanceLogDto;
import com.hrms.dto.WorkerDto;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceService {

	AttendanceLogDto clockIn(Long workerId, Long siteId);

	AttendanceLogDto clockOut(Long workerId);

	List<WorkerDto> getActiveWorkers();

	Page<AttendanceLogDto> getAttendanceHistory(Long workerId, int page, int size);

	Page<AttendanceLogDto> getAttendanceByDateRange(Long workerId, LocalDateTime from, LocalDateTime to, int page,
			int size);

	List<AttendanceLogDto> getFlaggedAttendance(Long workerId);
}