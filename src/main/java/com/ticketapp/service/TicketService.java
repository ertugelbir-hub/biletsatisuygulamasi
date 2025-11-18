package com.ticketapp.service;

import com.ticketapp.dto.PurchaseRequest;
import com.ticketapp.entity.Event;
import com.ticketapp.entity.Ticket;
import com.ticketapp.entity.User;
import com.ticketapp.exception.ErrorMessages;
import com.ticketapp.exception.ResourceNotFoundException;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.TicketRepository;
import com.ticketapp.repository.UserRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private final EventRepository eventRepo;
    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;

    public TicketService(EventRepository eventRepo,
                         TicketRepository ticketRepo,
                         UserRepository userRepo) {
        this.eventRepo = eventRepo;
        this.ticketRepo = ticketRepo;
        this.userRepo = userRepo;
    }

    /** Kaç defa tekrar denesin? Yüksek trafik için 3–5 yeterli */
    private static final int MAX_RETRY = 3;

    @Transactional
    public Ticket purchase(PurchaseRequest r, String username) {
        // 1) quantity validation
        if (r.quantity <= 0) {
            throw new RuntimeException(ErrorMessages.INVALID_QUANTITY);
        }

        // 2) Kullanıcıyı bul
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // 3) Event'i bul
        Event event = eventRepo.findById(r.eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.EVENT_NOT_FOUND));

        // 4) Optimistic locking ile satın alma
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                // 1) Event oku (güncel hali)
                Event e = eventRepo.findById(r.eventId)
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.EVENT_NOT_FOUND));

                int sold = ticketRepo.sumQuantityByEventId(e.getId());
                int remaining = e.getTotalSeats() - sold;

                if (remaining < r.quantity) {
                    throw new RuntimeException(ErrorMessages.NO_SEATS_LEFT + " (kalan: " + remaining + ")");
                }

                // 3) Bileti oluştur
                Ticket t = new Ticket();
                t.setEvent(e);
                t.setUsername(username);
                t.setQuantity(r.quantity);
                t.setCreatedAt(LocalDateTime.now());

                // 4) Kaydet ve dön
                return ticketRepo.save(t);

            } catch (OptimisticLockingFailureException ex) {
                // Versiyon çakışması olursa tekrar dene
                if (attempt == MAX_RETRY) {
                    throw new RuntimeException(ErrorMessages.RETRY_FAILED);
                }
                try {
                    Thread.sleep(100L * attempt);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Normalde buraya düşmez ama derleyici için:
        throw new RuntimeException(ErrorMessages.PURCHASE_FAILED);
    }



    public List<Ticket> listAll() {
        return ticketRepo.findAll();
    }

    public List<Ticket> listByUsername(String username) {
        return ticketRepo.findByUsername(username);
    }

    @Transactional
    public void cancel(Long ticketId, String username) {
        Ticket t = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.TICKET_NOT_FOUND));
        boolean owner = t.getUsername().equalsIgnoreCase(username);

        // Kullanıcının yetkilerinde ROLE_ADMIN var mı?
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean admin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!owner && !admin) {
            throw new RuntimeException(ErrorMessages.TICKET_CANCEL_FORBIDDEN);
        }

        // totalSeats'i değiştirmiyoruz; satış sum(quantity) ile hesaplanıyor.
        ticketRepo.deleteById(ticketId);
    }

    public List<Ticket> myTickets(String username) {
        return ticketRepo.findByUsername(username);
    }
}
