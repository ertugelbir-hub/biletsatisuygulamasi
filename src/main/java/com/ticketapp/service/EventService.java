package com.ticketapp.service;

import com.ticketapp.dto.EventRequest;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.entity.Event;
import com.ticketapp.exception.ResourceNotFoundException;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import java.util.ArrayList;
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
        Event e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik bulunamadı"));
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

        int sold = ticketRepo.sumQuantityByEventId(e.getId());
        int remaining = e.getTotalSeats() - sold;
        if (remaining < 0) remaining = 0;

        BigDecimal price = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
        BigDecimal revenue = price.multiply(BigDecimal.valueOf(sold));

        return new SalesReport(
                e.getId(),
                e.getTitle(),
                e.getCity(),
                e.getVenue(),
                e.getDateTime(),
                e.getTotalSeats(),
                sold,
                remaining,
                price,
                revenue
        );
    }
    public Page<Event> listPaged(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Page<Event> search(String city, String type, String q,
                              LocalDateTime from, LocalDateTime to,
                              Pageable pageable) {
        return repo.search(city, type, q, from, to, pageable);
    }
    public List<SalesReport> salesSummary(LocalDateTime from, LocalDateTime to) {
        // 1) Aralıktaki etkinlikleri çek
        Page<Event> page = repo.search(null, null, null, from, to, PageRequest.of(0, 1000));
        List<Event> events = page.getContent();

        // 2) Her event için sold/remaining/revenue hesapla
        List<SalesReport> list = new ArrayList<>();
        for (Event e : events) {
            int sold = ticketRepo.sumQuantityByEventId(e.getId()); // toplam satış adedi
            int remaining = Math.max(0, e.getTotalSeats() - sold); // kapasite - satılan
            BigDecimal price = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
            BigDecimal revenue = e.getPrice().multiply(BigDecimal.valueOf(sold)); // ciro

            list.add(new SalesReport(
                    e.getId(),
                    e.getTitle(),
                    e.getCity(),
                    e.getVenue(),
                    e.getDateTime(),
                    e.getTotalSeats(),
                    sold,
                    remaining,
                    price,
                    revenue
            ));
        }
        return list;
    }


}

