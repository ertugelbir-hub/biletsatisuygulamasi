package com.ticketapp.controller;

import com.ticketapp.service.ReportService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class)
@AutoConfigureMockMvc(addFilters = false)   // Security filtrelerini kapat
class ReportControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ReportService reportService;            // servis stub
    @MockBean
    com.ticketapp.security.JwtAuthFilter jwtAuthFilter;

    @MockBean
    com.ticketapp.security.JwtService jwtService;

    @Test
    void fullCsv_admin_ok() throws Exception {
        byte[] csv = "eventId,title\n1,Test".getBytes(StandardCharsets.UTF_8);

        Mockito.when(reportService.fullCsv(Mockito.any(), Mockito.any()))
                .thenReturn(csv);
        Mockito.when(reportService.fullCsvFilename(Mockito.any(), Mockito.any()))
                .thenReturn("full-sales-report.csv");

        mvc.perform(get("/api/reports/sales/full.csv")
                        .param("from", "2025-12-01T00:00")
                        .param("to", "2025-12-31T23:59"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(".csv")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(startsWith("eventId,title")));
    }

    @Test
    void fullPdf_admin_ok() throws Exception {
        byte[] pdf = "%PDF-1.4\n".getBytes(StandardCharsets.UTF_8);

        Mockito.when(reportService.fullPdf(Mockito.any(), Mockito.any()))
                .thenReturn(pdf);
        Mockito.when(reportService.fullPdfFilename(Mockito.any(), Mockito.any()))
                .thenReturn("full-sales-report.pdf");

        mvc.perform(get("/api/reports/sales/full.pdf")
                        .param("from", "2025-12-01T00:00")
                        .param("to", "2025-12-31T23:59"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(".pdf")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF.toString()))
                .andExpect(content().bytes(pdf));
    }
//    @Test
//    void full_admin_required_403() throws Exception {
//        // ADMIN değil -> 403
//        mvc.perform(get("/api/reports/sales/full")
//                        .with(user("alice").roles("USER"))
//                        .with(csrf()))
//                .andExpect(status().isForbidden());
//    }
}
