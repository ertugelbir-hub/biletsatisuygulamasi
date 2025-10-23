package com.ticketapp.config;

import com.ticketapp.entity.User;
import com.ticketapp.repository.UserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedConfig {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepo, PasswordEncoder encoder) {
        return args -> {
            if (userRepo.findByUsername("admin") == null) {
                User u = new User();
                u.setUsername("admin");
                u.setPassword(encoder.encode("admin123")); // giriş şifresi
                u.setEmail("admin@example.com");
                u.setRole("ADMIN");                       // kritik: token’da ROLE_ADMIN olsun
                userRepo.save(u);
                System.out.println("Seed -> admin/admin123 oluşturuldu.");
            }
        };
    }
}
