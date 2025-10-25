package com.ticketapp.controller;

import com.ticketapp.dto.EventRequest;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.entity.Event;
import com.ticketapp.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Validated // <-- Parametre doğrulamaları için şart
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }
    // CREATE (body doğrulaması)
    @PostMapping //admin
    public ResponseEntity<Event> create(@RequestBody @Valid EventRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping
    public ResponseEntity<List<Event>> list() {
        return ResponseEntity.ok(service.list());
    }

    @PutMapping("/{id}")      // ADMIN
    public ResponseEntity<Event> update(@PathVariable Long id, @RequestBody EventRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")   // ADMIN
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Silindi");
    }
    @GetMapping("/reports/sales")
    public ResponseEntity<SalesReport> sales(@RequestParam Long eventId) {
        return ResponseEntity.ok(service.salesReport(eventId));
    } // LIST paged (parametre doğrulaması)
    @GetMapping("/paged")   // <--- ÖNEMLİ: /api/events/paged olur
    public ResponseEntity<Page<Event>> listPaged(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] parts = sort.split(",");
        Sort s = Sort.by(Sort.Direction.fromString(parts.length > 1 ? parts[1] : "asc"), parts[0]);
        Page<Event> result = service.listPaged(PageRequest.of(page, size, s));
        return ResponseEntity.ok(result);

    }



}


