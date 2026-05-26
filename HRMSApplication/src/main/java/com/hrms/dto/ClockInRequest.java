package com.hrms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClockInRequest {
    @NotNull(message = "workerId cannot be null")
    private Long workerId;
    
    @NotNull(message = "siteId cannot be null")
    private Long siteId;
}
