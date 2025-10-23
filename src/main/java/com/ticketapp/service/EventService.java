package com.ticketapp.service;

import com.ticketapp.dto.EventRequest;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.entity.Event;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EventService {
    private final EventRepository repo;
    private final TicketRepository ticketRepo;
    public EventService(EventRepository repo, TicketRepository ticketRepo) {
        this.repo = repo;
        this.ticketRepo = ticketRepo;
    }


    public Event create(EventRequest r) {
        Event e = new Event();
        e.setTitle(r.title);
        e.setCity(r.city);
        e.setType(r.type);
        e.setVenue(r.venue);
        e.setDateTime(r.getDateTime());
        e.setTotalSeats(r.totalSeats);
        e.setPrice(r.price);
        return repo.save(e);
    }


    public List<Event> list() {
        return repo.findAll();
    }
    public Event update(Long id, EventRequest r) {
        Event e = repo.findById(id).orElseThrow(() -> new RuntimeException("Event bulunamadı"));
        e.setTitle(r.getTitle());
        e.setCity(r.getCity());
        e.setType(r.getType());
        e.setVenue(r.getVenue());
        e.setDateTime(r.getDateTime());
        e.setTotalSeats(r.getTotalSeats());
        e.setPrice(r.getPrice());
        return repo.save(e);
    }
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Event bulunamadı");
        }
        repo.deleteById(id);
    }
    public SalesReport salesReport(Long eventId) {
        Event e = repo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event bulunamadı"));

        int sold = ticketRepo.sumQuantityByEventId(eventId);
        int remaining = e.getTotalSeats() - sold;
        if (remaining < 0) remaining = 0;

        BigDecimal revenue = e.getPrice().multiply(new BigDecimal(sold));

        return new SalesReport(e.getId(), e.getTitle(), e.getTotalSeats(), sold, remaining, revenue);
    }

}

