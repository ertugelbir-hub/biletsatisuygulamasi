// src/main/java/com/ticketapp/service/ReportService.java
package com.ticketapp.service;

import com.ticketapp.dto.SalesReport;
import com.ticketapp.dto.SalesSummaryItem;
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
    // A) Purchase time

    public List<SalesSummaryItem> summaryByPurchase(LocalDateTime from, LocalDateTime to) {
        return eventRepo.findAll().stream().map(e -> {
            int soldInRange = ticketRepo.sumQuantityByEventIdBetweenPurchase(e.getId(), from, to);
            int allTimeSold = ticketRepo.sumQuantityByEventId(e.getId());
            int remaining   = Math.max(0, e.getTotalSeats() - allTimeSold);

            var price   = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
            var revenue = price.multiply(BigDecimal.valueOf(soldInRange));

            return new SalesSummaryItem(
                    e.getId(), e.getTitle(), e.getTotalSeats(),
                    soldInRange, remaining, price, revenue
            );
        }).toList();
    }
    // B) Event date
    public List<SalesSummaryItem> summaryByEvent(LocalDateTime from, LocalDateTime to) {
        return eventRepo.findAll().stream().map(e -> {
            int soldInRange = ticketRepo.sumQuantityByEventIdBetweenEventDate(e.getId(), from, to);
            int allTimeSold = ticketRepo.sumQuantityByEventId(e.getId());
            int remaining   = Math.max(0, e.getTotalSeats() - allTimeSold);

            var price   = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
            var revenue = price.multiply(BigDecimal.valueOf(soldInRange));

            return new SalesSummaryItem(
                    e.getId(), e.getTitle(), e.getTotalSeats(),
                    soldInRange, remaining, price, revenue
            );
        }).toList();
    }
    // C) All time
    public SalesReport allTimeSales(Long eventId) {
        var e = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));

        int soldAllTime = ticketRepo.sumQuantityByEventId(e.getId());
        int remaining   = Math.max(0, e.getTotalSeats() - soldAllTime);

        var price   = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
        var revenue = price.multiply(BigDecimal.valueOf(soldAllTime));

        return new SalesReport(
                e.getId(), e.getTitle(), e.getCity(), e.getVenue(),
                e.getDateTime(), e.getTotalSeats(),
                soldAllTime, remaining, price, revenue
        );
    }

}
