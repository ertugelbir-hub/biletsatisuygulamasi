// src/main/java/com/ticketapp/service/ReportService.java
package com.ticketapp.service;

import com.ticketapp.dto.FullSalesReport;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.dto.SalesSummaryItem;
import com.ticketapp.entity.Event;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    /** Birleşik rapor: tarih aralığı + all-time metrikler tek listede */
    public List<FullSalesReport> full(LocalDateTime from, LocalDateTime to) {
        // Güvenlik: from > to geldiyse takas et
        LocalDateTime f = from;
        LocalDateTime t = to;
        if (f != null && t != null && f.isAfter(t)) {
            LocalDateTime tmp = f; f = t; t = tmp;
        }
        // 2) tüm etkinlikleri çek
        List<Event> events = eventRepo.findAll();
        List<FullSalesReport> out = new ArrayList<>(events.size());
        // 3) her etkinlik için metrikleri hesapla
        for (Event e : events) {
            Long eventId = e.getId();

            // all-time satış
            int soldAllTime = ticketRepo.sumQuantityByEventId(eventId);
            if (soldAllTime < 0) soldAllTime = 0;

            // tarih aralığı satışı (from/to ikisi de verilmişse)
            int soldInRange = 0;
            if (f != null && t != null) {
                soldInRange = ticketRepo.sumQuantityByEventIdBetweenPurchase(eventId, f, t);
                if (soldInRange < 0) soldInRange = 0;
            }
            // kalan koltuk
            int totalSeats = e.getTotalSeats();
            int remaining = totalSeats - soldAllTime;
            if (remaining < 0) remaining = 0;

            // fiyat ve gelirler
            BigDecimal price = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
            BigDecimal revenueRange = price.multiply(BigDecimal.valueOf(soldInRange));
            BigDecimal revenueAllTime = price.multiply(BigDecimal.valueOf(soldAllTime));
            // DTO
            out.add(new FullSalesReport(
                    eventId,
                    e.getTitle(),
                    totalSeats,
                    soldInRange,
                    soldAllTime,
                    remaining,
                    price,
                    revenueRange,
                    revenueAllTime
            ));
        }

        return out;
    }
}

