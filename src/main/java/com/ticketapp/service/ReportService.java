// src/main/java/com/ticketapp/service/ReportService.java
package com.ticketapp.service;

import com.ticketapp.dto.SalesSummaryItem;
import com.ticketapp.entity.Event;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {
//Yeni bir servis oluşturuyoruz: tüm event’leri dolaş, her biri için sold/remaining/revenue hesapla.
    private final EventRepository eventRepo;
    private final TicketRepository ticketRepo;

    public ReportService(EventRepository eventRepo, TicketRepository ticketRepo) {
        this.eventRepo = eventRepo;
        this.ticketRepo = ticketRepo;
    }

    public List<SalesSummaryItem> salesSummary(LocalDateTime from, LocalDateTime to) {
        // tüm event’leri çek (ileride filtre eklersin)
        List<Event> events = eventRepo.findAll();
        List<SalesSummaryItem> items = ticketRepo.summaryByEventDate(from, to);
// Java tarafında revenue = price * sold
        for (SalesSummaryItem i : items) {
            if (i.price == null) {
                i.price = BigDecimal.ZERO;
            }
            i.revenue = i.price.multiply(BigDecimal.valueOf(i.sold));
        }
        return items;

    }
}
