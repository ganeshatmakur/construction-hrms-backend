package com.hrms.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.dto.WorkerDto;
import com.hrms.service.CacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheServiceImpl implements CacheService {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	@Value("${app.cache.active-workers-ttl-hours:16}")
	private int activeworkersTtlHours;

	private static final String ACTIVE_WORKERS_KEY = "active_workers";
	private static final String ACTIVE_WORKERS_TTL_KEY = "active_workers_ttl";

	@Override
	public void addActiveWorker(Long workerId, String workerName, String siteName, LocalDateTime clockInTime) {

		try {
			Map<String, Object> workerData = new HashMap<>();
			workerData.put("workerId", workerId);
			workerData.put("name", workerName);
			workerData.put("site", siteName);
			workerData.put("clockInTime", clockInTime.toString());

			String json = objectMapper.writeValueAsString(workerData);

			// Add to hash set
			redisTemplate.opsForHash().put(ACTIVE_WORKERS_KEY, workerId.toString(), json);

			// Set TTL on the hash key
			redisTemplate.expire(ACTIVE_WORKERS_KEY, activeworkersTtlHours, TimeUnit.HOURS);

			log.debug("[v0] Added active worker to cache: workerId={}, site={}", workerId, siteName);
		} catch (Exception e) {
			log.error("[v0] Failed to add active worker to Redis cache: {}", e.getMessage());
			throw new RuntimeException("Cache operation failed", e);
		}

	}

	@Override
	public void removeActiveWorker(Long workerId) {

		try {
			Long removed = redisTemplate.opsForHash().delete(ACTIVE_WORKERS_KEY, workerId.toString());
			log.debug("[v0] Removed worker from cache: workerId={}, removed={}", workerId, removed > 0);
		} catch (Exception e) {
			log.error("[v0] Failed to remove worker from Redis cache: {}", e.getMessage());
			throw new RuntimeException("Cache operation failed", e);
		}

	}

	@Override
	public List<WorkerDto> getActiveWorkers() {

		try {
			Map<String, String> entries = (Map) redisTemplate.opsForHash().entries(ACTIVE_WORKERS_KEY);
			if (entries == null || entries.isEmpty()) {
				log.debug("[v0] No active workers in cache");
				return List.of();
			}

			List<WorkerDto> activeWorkers = new ArrayList<>();
			for (Object value : entries.values()) {
				try {
					Map<String, Object> workerData = objectMapper.readValue((String) value, Map.class);

					WorkerDto dto = WorkerDto.builder().id(Long.valueOf(workerData.get("workerId").toString()))
							.name((String) workerData.get("name")).build();
					activeWorkers.add(dto);
				} catch (Exception e) {
					log.warn("[v0] Failed to parse cached worker entry: {}", e.getMessage());
				}
			}

			log.debug("[v0] Retrieved {} active workers from cache", activeWorkers.size());
			return activeWorkers;
		} catch (Exception e) {
			log.error("[v0] Failed to retrieve active workers from Redis: {}", e.getMessage());
			// Graceful degradation: return empty list instead of crashing
			return List.of();
		}

	}

	@Override
	public void invalidateAllActiveWorkers() {

		try {
			redisTemplate.delete(ACTIVE_WORKERS_KEY);
			log.info("[v0] Invalidated all active workers cache");
		} catch (Exception e) {
			log.warn("[v0] Failed to invalidate cache: {}", e.getMessage());
		}

	}

	@Override
	public boolean isRedisAvailable() {

		try {
			String result = redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());

			return "PONG".equalsIgnoreCase(result);

		} catch (Exception e) {
			log.warn("[v0] Redis health check failed: {}", e.getMessage());
			return false;
		}
	}

	@Override
	public long getActiveWorkersCount() {

		try {
			Long size = redisTemplate.opsForHash().size(ACTIVE_WORKERS_KEY);
			return size != null ? size : 0;
		} catch (Exception e) {
			log.warn("[v0] Failed to get active workers count: {}", e.getMessage());
			return 0;
		}
	}

}
