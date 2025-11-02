package com.ticketapp.dto;

import java.math.BigDecimal;

public class FullSalesReport {
    public Long eventId;
    public String title;
    public int totalSeats;

    public int soldInRange;     // from-to aralığı satışı
    public int soldAllTime;     // tüm zaman satışı
    public int remaining;       // totalSeats - allTime(sold)
    public BigDecimal price;

    public BigDecimal revenueRange;    // soldInRange * price
    public BigDecimal revenueAllTime;  // soldAllTime * price

    public FullSalesReport() { }

    public FullSalesReport(Long eventId, String title, int totalSeats,
                           int soldInRange, int soldAllTime, int remaining,
                           BigDecimal price, BigDecimal revenueRange, BigDecimal revenueAllTime) {
        this.eventId = eventId;
        this.title = title;
        this.totalSeats = totalSeats;
        this.soldInRange = soldInRange;
        this.soldAllTime = soldAllTime;
        this.remaining = remaining;
        this.price = price;
        this.revenueRange = revenueRange;
        this.revenueAllTime = revenueAllTime;
    }
}
