package com.ticketapp.service;

import com.ticketapp.dto.EventRequest;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.entity.Event;
import com.ticketapp.exception.ErrorMessages;
import com.ticketapp.exception.ResourceNotFoundException;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class EventService {

    // Havalı varsayılan resimler (Unsplash'tan)
    private static final String[] DEFAULT_IMAGES = {
            "https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?auto=format&fit=crop&w=800&q=80",
            "https://cdn.bubilet.com.tr/cdn-cgi/image/format=auto,width=828/https://cdn.bubilet.com.tr/files/Blog/resim-adi-37993.jpg",
            "https://blog.biletino.com/wp-content/uploads/2022/03/Konser-nedir-biletino-11245.jpg",
            "https://fs.galataport.com/cdn/uploads/000003402_1.png"
    };

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
        if (r.getImageUrl() != null && !r.getImageUrl().isBlank()) {
            // Admin resim verdiyse onu kullan
            e.setImageUrl(r.getImageUrl());
        } else {
            // Vermediyse rastgele bir tane seç
            int randomIndex = new Random().nextInt(DEFAULT_IMAGES.length);
            e.setImageUrl(DEFAULT_IMAGES[randomIndex]);
        }
        return repo.save(e);
    }


    public List<Event> list() {
        return repo.findAll();
    }
    public Event update(Long id, EventRequest r) {
        Event e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.EVENT_NOT_FOUND));
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
            throw new RuntimeException(ErrorMessages.EVENT_NOT_FOUND);
        }
        repo.deleteById(id);
    }

    public SalesReport salesReport(Long eventId) {
        Event e = repo.findById(eventId)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.EVENT_NOT_FOUND));

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

