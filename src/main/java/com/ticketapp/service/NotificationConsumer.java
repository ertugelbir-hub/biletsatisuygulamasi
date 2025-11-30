package com.ticketapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketapp.dto.TicketNotificationEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket için
    private final EmailService emailService;               // Mail için

    public NotificationConsumer(ObjectMapper objectMapper,
                                SimpMessagingTemplate messagingTemplate,
                                EmailService emailService) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
    }

    @KafkaListener(topics = "notification-topic", groupId = "ticket-notification-group")
    public void listen(String message) {
        try {
            // 1. Gelen JSON'ı Java Nesnesine çevir
            TicketNotificationEvent event = objectMapper.readValue(message, TicketNotificationEvent.class);

            // -----------------------------------------------------------
            // GÖREV 1: KONSOLA HAVALI RAPOR BAS (Senin İstediğin) 📊
            // -----------------------------------------------------------
            System.out.println("=========================================");
            System.out.println("📬 KAFKA RAPORU GELDİ (İşleniyor...)");
            System.out.println("-----------------------------------------");
            System.out.println("👤 Alan Kişi : " + event.getUsername());
            System.out.println("🎫 Etkinlik  : " + event.getEventTitle());
            System.out.println("🔢 Adet      : " + event.getQuantity());
            System.out.println("💰 Tutar     : " + event.getTotalPrice() + " ₺");
            System.out.println("📉 Kalan Stok: " + event.getRemainingSeats());
            System.out.println("🔥 Son 24s   : " + event.getSoldLast24Hours() + " bilet satıldı!");
            System.out.println("=========================================");

            // -----------------------------------------------------------
            // GÖREV 2: WEBSOCKET İLE CANLI YAYIN 📡
            // -----------------------------------------------------------
            // Frontend'e "Stok güncellendi" haberi uçur
            messagingTemplate.convertAndSend("/topic/sales", event);

            // -----------------------------------------------------------
            // GÖREV 3: MAİL GÖNDERME (Mailtrap) 📧
            // -----------------------------------------------------------
            String mailBaslik = "Biletiniz Hazır! 🎟️ - " + event.getEventTitle();

            // Mail içeriğine de istatistikleri koyalım ki zengin olsun
            String mailIcerigi = String.format("""
                Merhaba %s,
                
                "%s" etkinliği için bilet işleminiz tamamlandı.
                
                --------------------------------------
                🎫 Bilet ID: %d
                🔢 Adet: %d
                💰 Toplam Tutar: %s ₺
                --------------------------------------
                
                📈 Etkinlik Durumu:
                Şu an kalan bilet sayısı: %d
                Son 24 saatte satılan: %d
                
                İyi eğlenceler dileriz!
                TicketApp Ekibi
                """,
                    event.getUsername(),
                    event.getEventTitle(),
                    event.getTicketId(),
                    event.getQuantity(),
                    event.getTotalPrice(),
                    event.getRemainingSeats(),
                    event.getSoldLast24Hours());

            // Maili gönder (Kullanıcının maili yoksa test mailine at)
            String emailTo = (event.getEmail() != null && !event.getEmail().isEmpty()) ? event.getEmail() : "test@example.com";
            emailService.sendTicketInfo(emailTo, mailBaslik, mailIcerigi);

            System.out.println("✅ MAİL GÖNDERİLDİ: " + emailTo);

        } catch (Exception e) {
            System.err.println("❌ Mesaj işleme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}