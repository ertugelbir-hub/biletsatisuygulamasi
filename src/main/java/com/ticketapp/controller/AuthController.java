package com.ticketapp.controller;

import com.ticketapp.dto.LoginRequest;
import com.ticketapp.dto.RegisterRequest;
import com.ticketapp.entity.User;
import com.ticketapp.security.JwtService;
import com.ticketapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController                                     // SPRING: HTTP istekleri, JSON dönüş
@RequestMapping("/api/auth")                        // SPRING: bu controller'ın base path'i
public class AuthController {

    private final UserService userService;          // SPRING: Service bean injection
    private final PasswordEncoder passwordEncoder; // <-- ekledik
    private final JwtService jwtService;        // <-- alan

    // SPRING: Constructor Injection — Spring burada UserService'i otomatik verir
    public AuthController(UserService userService,
                          PasswordEncoder passwordEncoder
                          , JwtService jwtService) { // <-- ekledik
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;           // <-- constructor injection
    }

    @PostMapping("/register")                       // SPRING: POST /api/auth/register
    public ResponseEntity<String> register(@RequestBody RegisterRequest req) {
        // Basit validasyon (boş username/password kabul etmiyoruz)
        if (req.getUsername() == null || req.getUsername().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("username ve password zorunlu");
        }

        // DTO -> Entity dönüşümü (User nesnesini oluşturuyoruz)
        User u = new User(
                req.getUsername(),
                req.getPassword(),                   // Not: sade versiyon — şimdilik düz metin
                req.getEmail(),
                (req.getRole() == null || req.getRole().isBlank()) ? "CUSTOMER" : req.getRole()
        );

        // İş kuralı + kayıt (username varsa hata fırlatır; yoksa DB'ye kaydeder)
        userService.registerUser(u);

        return ResponseEntity.ok("Kayıt başarılı");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("username ve password zorunlu");
        }

        User u = userService.findByUsername(req.getUsername());
        if (u == null) {
            return ResponseEntity.status(401).body("Kullanıcı bulunamadı");
        }
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            return ResponseEntity.status(401).body("Şifre hatalı");

        }
        String token = jwtService.generate(u.getUsername(), u.getRole());
        return ResponseEntity.ok(Map.of("token", token));
    }

}
