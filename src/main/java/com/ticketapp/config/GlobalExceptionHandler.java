package com.ticketapp.config;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1) @Valid (DTO/body) ihlalleri
    // @Valid body hataları
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> onValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "validation");

        List<Map<String, String>> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", defaultMsg(fe)
                ))
                .collect(Collectors.toList());

        body.put("details", details);
        return ResponseEntity.badRequest().body(body);
    }

    private String defaultMsg(FieldError fe) {
        if ("dateTime".equals(fe.getField())) {
            return "Tarih formatı yyyy-MM-dd'T'HH:mm olmalı. Örnek: 2025-12-10T20:00";
        }// Mesaj boş gelirse default
        return fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Geçersiz değer";
    }


    // 2) @Validated + @RequestParam/@PathVariable ihlalleri
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> onConstraint(ConstraintViolationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "constraint");

        List<Map<String, String>> details = ex.getConstraintViolations()
                .stream()
                .map(v -> Map.of(
                        "property", v.getPropertyPath().toString(),
                        "message", v.getMessage()
                ))
                .collect(Collectors.toList());

        body.put("details", details);
        return ResponseEntity.badRequest().body(body);
    }

    // 3) Uygulama içi fırlattığın hatalar (ör. throw new RuntimeException(...))
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> onRuntime(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "runtime");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
