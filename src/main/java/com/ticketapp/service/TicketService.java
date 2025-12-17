package com.ticketapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketapp.dto.PurchaseRequest;
import com.ticketapp.dto.TicketNotificationEvent;
import com.ticketapp.entity.Event;
import com.ticketapp.entity.Seat;
import com.ticketapp.entity.Ticket;
import com.ticketapp.entity.User;
import com.ticketapp.exception.ErrorMessages;
import com.ticketapp.exception.ResourceNotFoundException;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.SeatRepository;
import com.ticketapp.repository.TicketRepository;
import com.ticketapp.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
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
    private final NotificationProducer notificationProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SeatRepository seatRepo;

    public TicketService(EventRepository eventRepo,
                         TicketRepository ticketRepo,
                         UserRepository userRepo,
                         NotificationProducer notificationProducer,
                         SeatRepository seatRepo) {
        this.eventRepo = eventRepo;
        this.ticketRepo = ticketRepo;
        this.userRepo = userRepo;
        this.notificationProducer = notificationProducer;
        this.seatRepo = seatRepo;
    }

    /** Kaç defa tekrar denesin? Yüksek trafik için 3–5 yeterli */
    private static final int MAX_RETRY = 3;

    @Transactional
    @CacheEvict(value = "sales_reports", key = "#r.eventId")
    public Ticket purchase(PurchaseRequest r, String username) {
        // 1) quantity validation
        if (r.quantity <= 0) {
            throw new RuntimeException(ErrorMessages.INVALID_QUANTITY);
        }

        // 2) Kullanıcıyı bul
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // 3) Event'i bul
        Event e = eventRepo.findById(r.eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.EVENT_NOT_FOUND));

        java.math.BigDecimal finalPrice = e.getPrice();
        // Eğer kupon kodu "YILBASI" ise %20 indir
        if ("YILBASI".equals(r.couponCode)) {
            finalPrice = finalPrice.multiply(java.math.BigDecimal.valueOf(0.80));
            System.out.println("🎄 YILBAŞI İNDİRİMİ UYGULANDI! Yeni Fiyat: " + finalPrice);
        }

        // 1. Durum: Eğer "YILBASI" kuponu kullanılıyorsa sınır 2'dir.
        if (r.couponCode != null && !r.couponCode.isEmpty()) {
            if (r.quantity > 2) {
                throw new RuntimeException("🏷️ İndirim kuponu ile en fazla 2 bilet alabilirsiniz!");
            }
        }
        // 2. Durum: Kupon yoksa veya başka bir kodsa sınır 6'dır.
        else {
            if (r.quantity > 6) {
                throw new RuntimeException("✋ Tek seferde en fazla 6 bilet alabilirsiniz!");
            }
        }

        // KOLTUK KONTROLLERİ
        if (r.seatIds == null || r.seatIds.isEmpty()) {
            throw new RuntimeException("Lütfen koltuk seçiniz.");
        }

        // 3) Optimistic locking (Retry Mekanizması - Senin istediğin yapı)
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                // Güncel veriyi çekelim
                List<Seat> selectedSeats = seatRepo.findAllById(r.seatIds);

                if (selectedSeats.size() != r.seatIds.size()) {
                    throw new RuntimeException("Seçilen bazı koltuklar bulunamadı.");
                }

                // Koltuklar dolu mu kontrol et?
                for (Seat seat : selectedSeats) {
                    if (seat.isSold()) {
                        throw new RuntimeException("Koltuk " + seat.getRowName() + seat.getSeatNumber() + " maalesef dolu! 😔");
                    }
                }

                // --- SATIŞ İŞLEMİ ---

                // A) Koltukları Dolu Yap
                for (Seat seat : selectedSeats) {
                    seat.setSold(true);
                    seatRepo.save(seat); // Burada versiyon çakışması olursa catch'e düşer
                }

                // B) Bileti Oluştur
                Ticket t = new Ticket();
                t.setEvent(e);
                t.setPrice(finalPrice);
                t.setUsername(username);
                t.setQuantity(r.seatIds.size());
                t.setCreatedAt(LocalDateTime.now());

                // Not: Senin sisteminde stok "total - satılan" olduğu için
                // event.setCurrentStock() çağırmıyoruz!

                Ticket savedTicket = ticketRepo.save(t);

                // --- KAFKA BİLDİRİM KISMI ---
                try {
                    // İstatistikleri hesapla
                    int totalSold = ticketRepo.sumQuantityByEventId(e.getId());
                    int remainingSeats = Math.max(0, e.getTotalSeats() - totalSold);

                    LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
                    int sold24h = ticketRepo.sumQuantityByEventIdBetweenPurchase(e.getId(), yesterday, LocalDateTime.now());

                    // DTO nesnesini oluştur
                    TicketNotificationEvent notificationEvent = new TicketNotificationEvent(
                            savedTicket.getId(),
                            e.getId(),
                            username,
                            e.getTitle(),
                            r.quantity,
                            finalPrice.multiply(java.math.BigDecimal.valueOf(r.quantity)),
                            user.getEmail(),
                            remainingSeats,
                            sold24h
                    );

                    // Producer'a nesneyi gönder (JSON dönüşümü orada yapılacak)
                    notificationProducer.sendNotification(notificationEvent);

                } catch (Exception ex) {
                    // Kafka hatası olsa bile bilet satışı iptal olmasın, sadece logla.
                    System.err.println("Kafka bildirim hatası: " + ex.getMessage());
                    ex.printStackTrace();
                }
                checkDynamicPricing(e);
                return savedTicket;



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
    // 👇 DİNAMİK FİYATLANDIRMA MANTIĞI
    private void checkDynamicPricing(Event event) {
        // 1. Şu ana kadar o etkinlik için satılan bilet sayısını bul
        int totalSold = ticketRepo.sumQuantityByEventId(event.getId());

        // 2. Doluluk oranını hesapla (Örn: 80/100 = 0.8)
        double occupancyRate = (double) totalSold / event.getTotalSeats();

        // 3. KURAL: Eğer doluluk %80'i (0.80) geçtiyse zam yap
        if (occupancyRate >= 0.80) {
            // Mevcut fiyatı al
            java.math.BigDecimal currentPrice = event.getPrice();

            // Fiyatı %10 artır (1.10 ile çarp)
            java.math.BigDecimal newPrice = currentPrice.multiply(java.math.BigDecimal.valueOf(1.10));

            // Yeni fiyatı kaydet
            event.setPrice(newPrice);
            eventRepo.save(event);

            System.out.println("📢 DİNAMİK FİYAT: " + event.getTitle() + " %80 doluluğu geçti! Yeni Fiyat: " + newPrice);
        }
    }

    public List<Ticket> myTickets(String username) {
        return ticketRepo.findByUsername(username);
    }
}
