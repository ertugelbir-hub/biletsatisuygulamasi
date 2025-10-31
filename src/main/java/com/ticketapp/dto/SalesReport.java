package com.ticketapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesReport {
    public Long eventId;
    public String title;
    public String city;
    public String venue;
    public LocalDateTime dateTime;
    public int totalSeats;
    public int sold;        // satılan koltuk adedi (quantity toplamı)
    public int remaining;   // totalSeats - sold
    public BigDecimal price;
    public BigDecimal revenue; // sold * event.price

    public SalesReport(Long eventId, String title,String city,String venue,LocalDateTime dateTime, int totalSeats, int sold, int remaining,  BigDecimal price,BigDecimal revenue) {
        this.eventId = eventId;
        this.title = title;
        this.city = city;
        this.venue = venue;
        this.dateTime = dateTime;
        this.totalSeats = totalSeats;
        this.sold = sold;
        this.remaining = remaining;
        this.price = price;
        this.revenue = revenue;
    }
}
