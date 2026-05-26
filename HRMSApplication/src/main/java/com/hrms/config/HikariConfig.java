package com.hrms.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

 
@Slf4j
@Configuration
public class HikariConfig {

	@Value("${spring.datasource.hikari.maximum-pool-size:20}")
	private int maximumPoolSize;

	@Value("${spring.datasource.hikari.minimum-idle:2}")
	private int minimumIdle;

	@Value("${spring.datasource.hikari.max-lifetime:1800000}")
	private long maxLifetime;

	@Value("${spring.datasource.hikari.keepalive-time:120000}")
	private long keepaliveTime;

	@Value("${spring.datasource.hikari.connection-timeout:30000}")
	private long connectionTimeout;

	@Value("${spring.datasource.hikari.leak-detection-threshold:60000}")
	private long leakDetectionThreshold;

	 
	@Bean
	DataSource hikariDataSource(DataSource dataSource) {
		if (dataSource instanceof HikariDataSource hikariDataSource) {

			// Ticket LF-205: Configure HikariCP for Supabase
			hikariDataSource.setMaximumPoolSize(maximumPoolSize);
			hikariDataSource.setMinimumIdle(minimumIdle);
			hikariDataSource.setMaxLifetime(maxLifetime);
			hikariDataSource.setKeepaliveTime(keepaliveTime);
			hikariDataSource.setConnectionTimeout(connectionTimeout);
			hikariDataSource.setLeakDetectionThreshold(leakDetectionThreshold);

			// Additional Supabase-specific settings
			hikariDataSource.setAutoCommit(true);
			hikariDataSource.setReadOnly(false);
			hikariDataSource.setValidationTimeout(connectionTimeout);

			logHikariConfig(hikariDataSource);

			return hikariDataSource;
		}

		log.warn("[v0] DataSource is not HikariDataSource, using as-is");
		return dataSource;
	}

	 
	private void logHikariConfig(HikariDataSource dataSource) {
		HikariConfigMXBean config = dataSource.getHikariConfigMXBean();

		log.info("[v0] HikariCP Configuration:");
		log.info("[v0]   - Maximum Pool Size: {}", config.getMaximumPoolSize());
		log.info("[v0]   - Minimum Idle: {}", config.getMinimumIdle());
		log.info("[v0]   - Max Lifetime: {}ms", config.getMaxLifetime());
		log.info("[v0]   - Keepalive Time: {}ms", dataSource.getKeepaliveTime());
		log.info("[v0]   - Connection Timeout: {}ms", config.getConnectionTimeout());
		log.info("[v0]   - Leak Detection Threshold: {}ms", config.getLeakDetectionThreshold());
		log.info("[v0]   - Auto Commit: {}", dataSource.isAutoCommit());
		log.info("[v0]   - Read Only: {}", dataSource.isReadOnly());
	}
}
