package com.ticketapp.controller;

import com.ticketapp.dto.PurchaseRequest;
import com.ticketapp.entity.Ticket;
import com.ticketapp.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static com.ticketapp.config.SwaggerExamples.ERROR_RES;
import static com.ticketapp.config.SwaggerExamples.TICKET_PURCHASE_REQ;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @Operation(summary = "Bilet satın al (USER)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Satın alım başarılı",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"status\":\"OK\" }")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Validasyon",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ERROR_RES)
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name="Purchase body", value = TICKET_PURCHASE_REQ)
            )
    )
    @PostMapping("/purchase")
    public ResponseEntity<Ticket> purchase(@RequestBody @Valid PurchaseRequest req,
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
    @GetMapping("/api/tickets/my")
    public ResponseEntity<List<Ticket>> myTickets(Principal principal) {
        List<Ticket> list = service.myTickets(principal.getName());
        return ResponseEntity.ok(list);
    }


}
