package com.ticketapp.controller;

import com.ticketapp.dto.LoginRequest;
import com.ticketapp.dto.RegisterRequest;
import com.ticketapp.entity.User;
import com.ticketapp.security.JwtService;
import com.ticketapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.ticketapp.config.SwaggerExamples.ERROR_RES;
import static com.ticketapp.config.SwaggerExamples.LOGIN_RES;
//Spring’de @Valid, kullanıcıdan gelen veriyi (request body) doğrulamak için kullanılır.
//Yani sen @NotBlank, @Positive, @NotNull gibi kuralları DTO’ya yazarsın,
//@Valid ise o kuralları aktif hale getirir.
@Tag(name = "Authentication", description = "Kayıt olma ve giriş işlemleri")
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
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest req) {
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

    @Operation(summary = "Giriş yap", description = "Kullanıcı adı ve şifre ile JWT token al.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Başarılı",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Login response", value = LOGIN_RES)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Yetkisiz",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ERROR_RES)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("username ve password zorunlu");
        }
        var maybeUser = userService.findByUsername(req.getUsername()); // Optional<User>
        var user = maybeUser.orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body("Kullanıcı bulunamadı");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Şifre hatalı");

        }
        String token = jwtService.generate(user.getUsername(), user.getRole());
        return ResponseEntity.ok(Map.of("token", token));
    }

}
