package com.ticketapp.dto;
/**
 * /api/auth/register endpoint'ine gelen JSON'u karşılayan DTO.
 * Controller, @RequestBody ile bu sınıfa otomatik map eder.
 *
 * Örnek JSON:
 * {
 *   "username": "mehmet",
 *   "password": "1234",
 *   "email": "m@example.com",
 *   "role": "CUSTOMER"
 * }
 */
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
public class RegisterRequest {
    @NotBlank(message = "Kullanıcı adı zorunludur") public String username;
    @NotBlank(message = "Şifre zorunludur ve 4 ile 64 basamak olmalıdır")
    @Size(min = 4, max = 64)
    private String password;
    private String email;
    private String role;
    // JSON -> Java çevirisi için parametresiz constructor şart
    public RegisterRequest() {}

    //getset
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
