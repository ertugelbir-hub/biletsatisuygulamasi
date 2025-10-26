package com.ticketapp.service;

import com.ticketapp.dto.PurchaseRequest;
import com.ticketapp.entity.Event;
import com.ticketapp.entity.Ticket;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {
    private final EventRepository eventRepo;
    private final TicketRepository ticketRepo;

    public TicketService(EventRepository eventRepo, TicketRepository ticketRepo) {
        this.eventRepo = eventRepo;
        this.ticketRepo = ticketRepo;
    }

    @Transactional
    public Ticket purchase(PurchaseRequest r,String username) {
        Event e = eventRepo.findById(r.eventId)
                .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));

        if (r.quantity <= 0) throw new RuntimeException("Adet 1 veya daha fazla olmalı");
        if (e.getTotalSeats() < r.quantity) throw new RuntimeException("Yeterli koltuk yok");

//        // Koltuk düş
//        e.setTotalSeats(e.getTotalSeats() - r.quantity);
//        eventRepo.save(e);

        // Bileti oluştur
        Ticket t = new Ticket();
        t.setEvent(e);
        t.setUsername(username);
        t.setQuantity(r.quantity);
        t.setCreatedAt(LocalDateTime.now().toString());

        return ticketRepo.save(t);
    }
    public List<Ticket> listAll() {
        return ticketRepo.findAll();
    }

    public List<Ticket> listByUsername(String username) {
        return ticketRepo.findAll()
                .stream()
                .filter(t -> t.getUsername().equalsIgnoreCase(username))
                .toList();
    }
    @Transactional
    public void cancel(Long ticketId, String username) {
        Ticket t = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Bilet bulunamadı"));

        if (!t.getUsername().equalsIgnoreCase(username))
            throw new RuntimeException("Bu bileti iptal etme yetkiniz yok");

        Event e = t.getEvent();
        e.setTotalSeats(e.getTotalSeats() + t.getQuantity());
        eventRepo.save(e);

        ticketRepo.deleteById(ticketId);
    }


}
