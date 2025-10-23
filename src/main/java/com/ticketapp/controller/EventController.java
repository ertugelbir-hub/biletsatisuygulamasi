package com.ticketapp.controller;

import com.ticketapp.dto.EventRequest;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.entity.Event;
import com.ticketapp.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @PostMapping //admin
    public ResponseEntity<Event> create(@RequestBody EventRequest req) {
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
    }

}


