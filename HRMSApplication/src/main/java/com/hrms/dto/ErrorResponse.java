package com.hrms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
	private String error;
	private String message;
	private int status;
	private LocalDateTime timestamp;
	private String path;
	private Map<String, Object> details;

	public static ErrorResponse of(String errorCode, String message, int status, String path) {
		return ErrorResponse.builder().error(errorCode).message(message).status(status).timestamp(LocalDateTime.now())
				.path(path).build();
	}

	public static ErrorResponse of(String errorCode, String message, int status, String path,
			Map<String, Object> details) {
		return ErrorResponse.builder().error(errorCode).message(message).status(status).timestamp(LocalDateTime.now())
				.path(path).details(details).build();
	}
}
