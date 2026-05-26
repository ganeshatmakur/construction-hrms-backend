package com.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceLogDto {
    private Long id;
    private WorkerDto worker;
    private SiteDto site;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private BigDecimal totalHours;
    private BigDecimal overtimeHours;
    private Boolean flagged;
    private LocalDateTime createdAt;
}
 
@Data
class SiteDto {
    private Long id;
    private String name;
    private String location;
}
