package com.ticketapp.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTicketInfo(String to, String title, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@ticketapp.com"); // Gönderen (Önemsiz)
            message.setTo(to);
            message.setSubject(title);
            message.setText(body);

            mailSender.send(message);
            System.out.println("✅ Mailtrap'e mail gönderildi: " + to);
        } catch (Exception e) {
            System.err.println("❌ Mail gönderme hatası: " + e.getMessage());
        }
    }
}