package com.ticketapp.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;     // Etkinlik adı
    private String city;      // Ankara, İstanbul...
    private String type;      // music, cinema, theatre, sport
    private String venue;     // Salon
    private LocalDateTime dateTime;
    private int totalSeats;   // Toplam koltuk
    private BigDecimal price;        // Basit olması için int

    public Event() {}

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
