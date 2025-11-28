package com.ticketapp.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Mesajı "notification-topic" adlı kutuya atar
    public void sendNotification(String message) {
        System.out.println("📤 Kafka'ya mesaj gönderiliyor: " + message);
        kafkaTemplate.send("notification-topic", message);
    }
}