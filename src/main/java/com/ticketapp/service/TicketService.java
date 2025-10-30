package com.ticketapp.service;

import com.ticketapp.dto.PurchaseRequest;
import com.ticketapp.entity.Event;
import com.ticketapp.entity.Ticket;
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
    public TicketService(EventRepository eventRepo, TicketRepository ticketRepo,UserRepository userRepo) {
        this.eventRepo = eventRepo;
        this.ticketRepo = ticketRepo;
        this.userRepo = userRepo;
    }
    /** Kaç defa tekrar denesin? Yüksek trafik için 3–5 yeterli */
    private static final int MAX_RETRY = 3;

    @Transactional
    public Ticket purchase(PurchaseRequest r,String username) {
        if (r.quantity <= 0) {
            throw new RuntimeException("Adet 1 veya daha fazla olmalı");
        }

        // (İsteğe bağlı) kullanıcıyı doğrula
        userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                // 1) Event oku
                Event e = eventRepo.findById(r.eventId)
                        .orElseThrow(() -> new RuntimeException("Etkinlik bulunamadı"));

                // 2) Kalan koltuk = totalSeats - sold
                int sold = ticketRepo.sumQuantityByEventId(e.getId());
                int remaining = e.getTotalSeats() - sold;

                if (remaining < r.quantity) {
                    throw new RuntimeException("Yeterli koltuk yok (kalan: " + remaining + ")");
                }
                //  3) Bileti oluştur
                Ticket t = new Ticket();
                t.setEvent(e);
                t.setUsername(username);
                t.setQuantity(r.quantity);
                t.setCreatedAt(LocalDateTime.now());

                return ticketRepo.save(t);
                // Not: versiyon çakışması olursa JPA flush’ta
                // OptimisticLockingFailureException fırlatır ve catch’e düşer.


            } catch (OptimisticLockingFailureException ex) {
                // çakışma
                if (attempt == MAX_RETRY) {
                    throw new RuntimeException("İşlem çakıştı, lütfen tekrar deneyin.");
                }
                // küçük bir bekleme (isteğe bağlı)
                try {
                    Thread.sleep(50L * attempt);
                } catch (InterruptedException ignored) {
                }
            }
        }
        throw new RuntimeException("Satın alma başarısız");
    }
    public List<Ticket> listAll() { return ticketRepo.findAll();
    }

    public List<Ticket> listByUsername(String username) {
        return ticketRepo.findByUsername(username);
    }
    @Transactional
    public void cancel(Long ticketId, String username) {
        Ticket t = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Bilet bulunamadı"));
        boolean owner = t.getUsername().equalsIgnoreCase(username);

        // Kullanıcının yetkilerinde ROLE_ADMIN var mı?
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean admin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!owner && !admin) {
            throw new RuntimeException("Bu bileti iptal etme yetkiniz yok");
        }
        // NOT: totalSeats'i değiştirmiyoruz. Satış "sum(quantity)" ile hesaplandığı için
        // bilet silmek zaten toplam satışı azaltır (iade etkisi otomatik yansır).
        ticketRepo.deleteById(ticketId);
//        Event e = t.getEvent();
//        e.setTotalSeats(e.getTotalSeats() + t.getQuantity());
//        eventRepo.save(e);


    }

    public List<Ticket> myTickets(String username) {
        return ticketRepo.findByUsername(username);
    }
}
