package com.ticketapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketapp.dto.TicketNotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper; // Java nesnesini JSON'a çevirir

    public NotificationProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // Metod artık String değil, TicketNotificationEvent alıyor
    public void sendNotification(TicketNotificationEvent event) {
        try {
            // Nesneyi JSON String'e çevir
            String jsonMessage = objectMapper.writeValueAsString(event);

            System.out.println("📤 [Kafka Producer] İstatistikli mesaj gönderiliyor...");

            kafkaTemplate.send("notification-topic", jsonMessage);

        } catch (JsonProcessingException e) {
            System.err.println("JSON hatası: " + e.getMessage());
        }
    }
}