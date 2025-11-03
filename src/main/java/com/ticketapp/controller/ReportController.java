package com.ticketapp.controller;

import com.ticketapp.dto.FullSalesReport;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.dto.SalesSummaryItem;
import com.ticketapp.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
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
    @ApiResponse(
            responseCode = "200",
            description = "Başarılı",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = FullSalesReport.class)),
                    examples = @ExampleObject(
                            name = "Örnek Liste",
                            value = "[{\n" +
                                    "  \"eventId\": 1,\n" +
                                    "  \"title\": \"Swagger Test Konseri\",\n" +
                                    "  \"totalSeats\": 10,\n" +
                                    "  \"soldInRange\": 0,\n" +
                                    "  \"soldAllTime\": 3,\n" +
                                    "  \"remaining\": 7,\n" +
                                    "  \"price\": 150,\n" +
                                    "  \"revenueRange\": 0,\n" +
                                    "  \"revenueAllTime\": 450\n" +
                                    "}]"
                    )
            )
    )
    @GetMapping("/sales/full")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FullSalesReport>> full(
            @Parameter(example = "2025-12-01T00:00")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime from,
            @Parameter(example = "2025-12-31T23:59")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime to
    ) {
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

        String filename = reportService.fullCsvFilename(from, to);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.set(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }


    @Operation(
            summary = "Birleşik rapor (sayfalı)",
            description = "Full raporu sayfalı döndürür. Varsayılan: page=0, size=10, sort=title, dir=asc"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Başarılı",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Page Örneği",
                            value = "{\n" +
                                    "  \"content\": [\n" +
                                    "    {\n" +
                                    "      \"eventId\": 1,\n" +
                                    "      \"title\": \"Swagger Test Konseri\",\n" +
                                    "      \"totalSeats\": 10,\n" +
                                    "      \"soldInRange\": 0,\n" +
                                    "      \"soldAllTime\": 3,\n" +
                                    "      \"remaining\": 7,\n" +
                                    "      \"price\": 150,\n" +
                                    "      \"revenueRange\": 0,\n" +
                                    "      \"revenueAllTime\": 450\n" +
                                    "    }\n" +
                                    "  ],\n" +
                                    "  \"pageable\": {\"pageNumber\": 0, \"pageSize\": 10, \"offset\": 0, \"paged\": true},\n" +
                                    "  \"last\": true,\n" +
                                    "  \"totalElements\": 1,\n" +
                                    "  \"totalPages\": 1,\n" +
                                    "  \"size\": 10,\n" +
                                    "  \"number\": 0,\n" +
                                    "  \"first\": true,\n" +
                                    "  \"numberOfElements\": 1,\n" +
                                    "  \"empty\": false\n" +
                                    "}"
                    )
            )
    )
    @GetMapping("/api/reports/sales/full/page")
    public ResponseEntity<Page<FullSalesReport>> fullPaged(
            @Parameter(example = "2025-12-01T00:00")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime from,
            @Parameter(example = "2025-12-31T23:59")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime to,
            @Parameter(example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(example = "title") @RequestParam(defaultValue = "title") String sort,
            @Parameter(example = "asc") @RequestParam(defaultValue = "asc") String dir
    ) {
        return ResponseEntity.ok(reportService.fullPaged(from, to, page, size, sort, dir));
    }
    @Operation(
            summary = "Birleşik raporu PDF indir",
            description = "Full raporu (tarih aralığı + all-time) PDF olarak döndürür."
    )
    @ApiResponse(responseCode = "200", description = "Başarılı")
    @GetMapping("/api/reports/sales/full.pdf")
    public ResponseEntity<byte[]> fullPdf(
            @Parameter(example = "2025-12-01T00:00")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime from,
            @Parameter(example = "2025-12-31T23:59")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime to
    ) {
        byte[] pdf = reportService.fullPdf(from, to);
        String filename = reportService.fullPdfFilename(from, to);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.set(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
