package com.ticketapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event implements Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // columnDefinition = "TEXT" veya "VARCHAR(255)" diyerek
    // veritabanına bunun kesinlikle YAZI olduğunu garanti ediyoruz.

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "VARCHAR(255)")
    private String venue;

    @Column(columnDefinition = "VARCHAR(255)")
    private String city;

    private LocalDateTime dateTime;

    private BigDecimal price;

    private Integer totalSeats;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column(columnDefinition = "VARCHAR(50)")
    private String type;

    @Version
    private Integer version;
}