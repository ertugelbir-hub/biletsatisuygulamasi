package com.ticketapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rowName; // A, B, C...
    private int seatNumber; // 1, 2, 3...

    private boolean isSold = false; // Dolu mu boş mu?

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @JsonIgnore // Sonsuz döngü olmasın diye event detayını gizliyoruz
    private Event event;

    public Seat() {}

    public Seat(String rowName, int seatNumber, Event event) {
        this.rowName = rowName;
        this.seatNumber = seatNumber;
        this.event = event;
        this.isSold = false;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public String getRowName() { return rowName; }
    public int getSeatNumber() { return seatNumber; }
    public boolean isSold() { return isSold; }
    public void setSold(boolean sold) { isSold = sold; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
}