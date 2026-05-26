package com.hrms.service;

import com.hrms.dto.WorkerDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CacheService {

	void addActiveWorker(Long workerId, String workerName, String siteName, LocalDateTime clockInTime);

	void removeActiveWorker(Long workerId);

	List<WorkerDto> getActiveWorkers();

	void invalidateAllActiveWorkers();

	boolean isRedisAvailable();

	long getActiveWorkersCount();
}