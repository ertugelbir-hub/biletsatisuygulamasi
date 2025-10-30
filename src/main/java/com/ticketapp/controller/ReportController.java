package com.ticketapp.controller;

import com.ticketapp.dto.SalesSummaryItem;
import com.ticketapp.service.ReportService;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController( ReportService reportService) { this.reportService = reportService;}
    // Örn: /api/reports/sales/summary?from=2025-12-01T00:00&to=2025-12-31T23:59
    @GetMapping("/sales/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesSummaryItem>> salesSummary(
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ){
        return ResponseEntity.ok(reportService.salesSummary(from, to));
    }

}
