package com.ticketapp.controller;

import com.ticketapp.dto.FullSalesReport;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.dto.SalesSummaryItem;
import com.ticketapp.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    @Operation(
            summary = "Satın alma tarihine göre satış özeti",
            description = "Verilen [from, to] aralığında satın alma tarihine göre sold/remaining/revenue bilgilerini döner."
    )

    // A) Satın alma tarihine göre özet
    @ApiResponse(responseCode = "200", description = "Başarılı")
    @GetMapping("/summary-by-purchase")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesSummaryItem>> summaryByPurchase(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.summaryByPurchase(from, to));
    }
    @Operation(
            summary = "Etkinlik tarihine göre satış özeti",
            description = "Verilen [from, to] aralığında tüm etkinlikler için sold/remaining/revenue bilgilerini döner."
    )
    // B) Etkinlik tarihine göre özet
    @ApiResponse(responseCode = "200", description = "Başarılı")
    @GetMapping("/summary-by-event")
    public ResponseEntity<List<SalesSummaryItem>> summaryByEvent(
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to)
    {
        return ResponseEntity.ok(reportService.summaryByEvent(from, to));
    }
    @Operation(
            summary = "All-time satış özeti (tek etkinlik)",
            description = "eventId ile tek bir etkinliğin tüm zaman satışlarını (sold/remaining/revenue) döner."
    )
    // GET /api/reports/sales/alltime?eventId=1
    @ApiResponse(responseCode = "200", description = "Başarılı")
    @GetMapping("/sales/alltime")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalesReport> allTime(@RequestParam Long eventId) {
        return ResponseEntity.ok(reportService.allTimeSales(eventId));
    }
    @Operation(
            summary = "Birleşik rapor: tarih aralığı + all-time",
            description = "Her etkinlik için hem [from,to] aralığındaki satışı hem de tüm zaman satışı ve gelirlerini birlikte döner."
    )
    @ApiResponse(responseCode = "200", description = "Başarılı")
    @GetMapping("/sales/full")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FullSalesReport>> full(

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.full(from, to));
    }
    @Operation(
            summary = "Birleşik raporu CSV indir",
            description = "Full raporu (tarih aralığı + all-time) CSV dosyası olarak döndürür."
    )
    @ApiResponse(responseCode = "200", description = "Başarılı")
    @GetMapping("/api/reports/sales/full.csv")
    public ResponseEntity<byte[]> fullCsv(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime to
    ) {
        byte[] csv = reportService.fullCsv(from, to);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"full-sales-report.csv\"");
        headers.set(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }


}
