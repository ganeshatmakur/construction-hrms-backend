package com.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private boolean success;
	private String message;
	private T data;
	private LocalDateTime timestamp;

	public static <T> ApiResponse<T> success(String message, T data) {
		return ApiResponse.<T>builder().success(true).message(message).data(data).timestamp(LocalDateTime.now())
				.build();
	}

	public static <T> ApiResponse<T> success(String message) {
		return ApiResponse.<T>builder().success(true).message(message).timestamp(LocalDateTime.now()).build();
	}

	public static <T> ApiResponse<T> error(String message) {
		return ApiResponse.<T>builder().success(false).message(message).timestamp(LocalDateTime.now()).build();
	}
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
class PaginatedResponse<T> {
	private boolean success;
	private String message;
	private java.util.List<T> content;
	private long totalElements;
	private int totalPages;
	private int currentPage;
	private int pageSize;
	private LocalDateTime timestamp;
}
