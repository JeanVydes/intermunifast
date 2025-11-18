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
import com.example.services.extra.MetricsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * Get dashboard metrics with optional date range
     * 
     * @param startDate Optional start date (defaults to start of current month)
     * @param endDate   Optional end date (defaults to now)
     * @return Dashboard metrics including cancellations, revenue, occupation,
     *         punctuality
     */
    @GetMapping("/dashboard")
    public ResponseEntity<MetricsDTO.DashboardMetrics> getDashboardMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start;
        LocalDateTime end;

        if (startDate == null) {
            // Default to start of current month
            start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        } else {
            start = startDate.atStartOfDay();
        }

        if (endDate == null) {
            // Default to now
            end = LocalDateTime.now();
        } else {
            end = endDate.atTime(LocalTime.MAX);
        }

        MetricsDTO.DashboardMetrics metrics = metricsService.getDashboardMetrics(start, end);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get metrics for today
     */
    @GetMapping("/dashboard/today")
    public ResponseEntity<MetricsDTO.DashboardMetrics> getTodayMetrics() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        MetricsDTO.DashboardMetrics metrics = metricsService.getDashboardMetrics(start, end);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get metrics for this week
     */
    @GetMapping("/dashboard/this-week")
    public ResponseEntity<MetricsDTO.DashboardMetrics> getThisWeekMetrics() {
        LocalDateTime start = LocalDate.now().minusDays((long) LocalDate.now().getDayOfWeek().getValue() - 1)
                .atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        MetricsDTO.DashboardMetrics metrics = metricsService.getDashboardMetrics(start, end);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get metrics for this month
     */
    @GetMapping("/dashboard/this-month")
    public ResponseEntity<MetricsDTO.DashboardMetrics> getThisMonthMetrics() {
        LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        MetricsDTO.DashboardMetrics metrics = metricsService.getDashboardMetrics(start, end);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get metrics for this year
     */
    @GetMapping("/dashboard/this-year")
    public ResponseEntity<MetricsDTO.DashboardMetrics> getThisYearMetrics() {
        LocalDateTime start = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        MetricsDTO.DashboardMetrics metrics = metricsService.getDashboardMetrics(start, end);
        return ResponseEntity.ok(metrics);
    }
}
