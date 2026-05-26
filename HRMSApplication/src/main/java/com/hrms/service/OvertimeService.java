package com.hrms.service;

import com.hrms.dto.OvertimeSummaryDto;
import com.hrms.entity.AttendanceLog;
import com.hrms.entity.Worker;

public interface OvertimeService {

	void createOvertimeEntry(AttendanceLog attendanceLog, Worker worker);

	OvertimeSummaryDto getMonthlySummary(Long workerId, String yearMonth);

	OvertimeSummaryDto settleOvertimeForMonth(Long workerId, String yearMonth);
}