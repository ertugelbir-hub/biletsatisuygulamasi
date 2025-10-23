package com.ticketapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EventRequest {
    public String title;
    public String city;
    public String type; // concert / theater / etc.
    public String venue;
    // JSON’dan tarih okurken formatı belirtelim:
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTime;
    public int totalSeats;
    public BigDecimal price;

    // Boş constructor (JSON -> DTO map’lemek için gerekli)
    public EventRequest() {}
    // (İsteğe bağlı) Tüm alanları alan constructor
    public EventRequest(String title, String city, String type, String venue,
                        LocalDateTime dateTime, int totalSeats, BigDecimal price) {
        this.title = title;
        this.city = city;
        this.type = type;
        this.venue = venue;
        this.dateTime = dateTime;
        this.totalSeats = totalSeats;
        this.price = price;
    }
    // ---- GETTER / SETTER’LAR ----
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}


