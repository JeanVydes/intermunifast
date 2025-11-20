package com.example.api.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.dto.MetricsDTO;
import com.example.metrics.MetricsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/dashboard")
    public ResponseEntity<MetricsDTO.DashboardMetrics> getDashboardMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start = startDate != null ? startDate.atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();

        return ResponseEntity.ok(metricsService.getDashboardMetrics(start, end));
    }

    @GetMapping("/dashboard/today")
    public ResponseEntity<MetricsDTO.DashboardMetrics> getTodayMetrics() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        return ResponseEntity.ok(metricsService.getDashboardMetrics(start, end));
    }

    @GetMapping("/dashboard/this-week")
    public ResponseEntity<MetricsDTO.DashboardMetrics> getThisWeekMetrics() {
        LocalDateTime start = LocalDate.now().minusDays((long) LocalDate.now().getDayOfWeek().getValue() - 1)
                .atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        return ResponseEntity.ok(metricsService.getDashboardMetrics(start, end));
    }

    @GetMapping("/dashboard/this-month")
    public ResponseEntity<MetricsDTO.DashboardMetrics> getThisMonthMetrics() {
        LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        return ResponseEntity.ok(metricsService.getDashboardMetrics(start, end));
    }

    @GetMapping("/dashboard/this-year")
    public ResponseEntity<MetricsDTO.DashboardMetrics> getThisYearMetrics() {
        LocalDateTime start = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        return ResponseEntity.ok(metricsService.getDashboardMetrics(start, end));
    }
}
