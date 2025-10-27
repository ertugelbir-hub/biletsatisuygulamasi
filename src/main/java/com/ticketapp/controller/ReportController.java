package com.ticketapp.controller;

import com.ticketapp.dto.SalesReport;
import com.ticketapp.service.EventService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final EventService eventService;
    public ReportController(EventService eventService) { this.eventService = eventService; }

    // zaten vardı: tek event için sales
    // @GetMapping("/sales") ...

    // yeni: tarih aralığına göre özet (çok etkinlik)
    @GetMapping("/sales/summary")
    public ResponseEntity<List<SalesReport>> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(eventService.salesSummary(from, to));
    }
}
