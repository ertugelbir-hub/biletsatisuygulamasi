package com.ticketapp.dto;
import jakarta.validation.constraints.*;
public class PurchaseRequest {
    @NotNull(message = "Etkinlik id girilmelidir") public Long eventId;
    @NotBlank(message = "Etkinlik id girilmelidir") public String username;
    @Positive(message = "Bilet sayısı pozitif olmalıdır")
    public int quantity;
    //Not: @NotBlank sadece String’ler için; Long, Integer gibi tiplerde @NotNull kullan.

}
