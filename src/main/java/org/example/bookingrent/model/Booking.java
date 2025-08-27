package org.example.bookingrent.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String productId;
    private int customerId;
    private int ownerId;
    private int quantity;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
