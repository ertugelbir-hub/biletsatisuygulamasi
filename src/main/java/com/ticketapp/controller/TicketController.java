package com.ticketapp.controller;

import com.ticketapp.dto.PurchaseRequest;
import com.ticketapp.entity.Ticket;
import com.ticketapp.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping("/purchase")
    public ResponseEntity<Ticket> purchase(@RequestBody PurchaseRequest req,
                                           Principal principal) {
        // principal.getName() = token’daki username
        return ResponseEntity.ok(service.purchase(req,principal.getName()));
    }
    @GetMapping
    public ResponseEntity<List<Ticket>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<Ticket>> listByUsername(@PathVariable String username) {
        return ResponseEntity.ok(service.listByUsername(username));
    }
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<?> cancel(@PathVariable Long ticketId, Principal principal) {
        service.cancel(ticketId, principal.getName());
        return ResponseEntity.ok("İptal edildi");
    }


}
