package com.ticketapp.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    // Kafka'yı sürekli dinler. Mesaj gelince burası çalışır.
    @KafkaListener(topics = "notification-topic", groupId = "ticket-notification-group")
    public void listen(String message) {
        // Burada gerçek hayatta JavaMailSender ile mail atılır.
        // Şimdilik simülasyon yapıyoruz:
        System.out.println("=========================================");
        System.out.println("📬 KAFKA'DAN MESAJ GELDİ (Simüle Edilen SMS/Mail):");
        System.out.println(message);
        System.out.println("=========================================");
    }
}