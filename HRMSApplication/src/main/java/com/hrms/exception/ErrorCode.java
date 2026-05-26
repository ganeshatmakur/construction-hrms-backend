
package com.hrms.exception;

import lombok.Getter;

 
@Getter
public enum ErrorCode {
    // Worker Errors
    WORKER_NOT_FOUND("W001", "Worker not found or inactive", 404),
    WORKER_ALREADY_EXISTS("W002", "Worker with this phone number already exists", 409),

    // Site Errors
    SITE_NOT_FOUND("S001", "Site not found or inactive", 404),
    SITE_ALREADY_EXISTS("S002", "Site with this name already exists", 409),

    // Attendance Errors
    ALREADY_CLOCKED_IN("A001", "Worker is already clocked in", 409),
    NOT_CLOCKED_IN("A002", "Worker is not currently clocked in", 404),
    INVALID_TIME("A003", "Invalid clock in/out time", 400),
    DUPLICATE_CLOCK_IN("A004", "Duplicate clock-in attempt within same hour", 409),

    // Overtime Errors
    CANNOT_SETTLE_CURRENT_MONTH("O001", "Cannot settle overtime for current month", 409),
    ALREADY_SETTLED("O002", "Overtime for this month is already settled", 409),
    INVALID_DATE_RANGE("O003", "Invalid date range for overtime query", 400),

    // Validation Errors
    VALIDATION_ERROR("V001", "Input validation failed", 400),
    MISSING_REQUIRED_FIELD("V002", "Required field is missing", 400),
    INVALID_REQUEST("V003", "Invalid request format", 400),

    // System Errors
    INTERNAL_SERVER_ERROR("E001", "Internal server error", 500),
    DATABASE_ERROR("E002", "Database operation failed", 500),
    CACHE_ERROR("E003", "Cache operation failed", 500),
    EXTERNAL_API_ERROR("E004", "External API call failed", 503);

    private final String code;
    private final String message;
    private final int httpStatus;

    ErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
