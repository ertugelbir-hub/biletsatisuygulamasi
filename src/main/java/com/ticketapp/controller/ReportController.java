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
@RequestMapping("/api/reports/sales")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {
    private final ReportService reportService;
    public ReportController( ReportService reportService) { this.reportService = reportService;}

    // A) Satın alma tarihine göre özet
    @GetMapping("/summary-by-purchase")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesSummaryItem>> summaryByPurchase(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.summaryByPurchase(from, to));
    }
    // B) Etkinlik tarihine göre özet
    @GetMapping("/summary-by-event")
    public ResponseEntity<List<SalesSummaryItem>> summaryByEvent(
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to)
    {
        return ResponseEntity.ok(reportService.summaryByEvent(from, to));
    }

}
