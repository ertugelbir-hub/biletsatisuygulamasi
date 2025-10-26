package com.ticketapp.dto;
import jakarta.validation.constraints.*;
public class PurchaseRequest {
    @NotNull(message = "Etkinlik id girilmelidir") @Positive(message = "eventId 1 veya daha büyük olmalı") public Long eventId;
    @Positive(message = "Bilet sayısı pozitif olmalıdır")public int quantity;
    //Not: @NotBlank sadece String’ler için; Long, Integer gibi tiplerde @NotNull kullan.

}
