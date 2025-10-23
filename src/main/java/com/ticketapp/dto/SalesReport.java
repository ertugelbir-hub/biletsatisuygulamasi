package com.ticketapp.dto;

import java.math.BigDecimal;

public class SalesReport {
    public Long eventId;
    public String title;
    public int totalSeats;
    public int sold;        // satılan koltuk adedi (quantity toplamı)
    public int remaining;   // totalSeats - sold
    public BigDecimal revenue; // sold * event.price

    public SalesReport(Long eventId, String title, int totalSeats, int sold, int remaining, BigDecimal revenue) {
        this.eventId = eventId;
        this.title = title;
        this.totalSeats = totalSeats;
        this.sold = sold;
        this.remaining = remaining;
        this.revenue = revenue;
    }
}
