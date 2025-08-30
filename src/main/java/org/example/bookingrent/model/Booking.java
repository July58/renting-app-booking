package org.example.bookingrent.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;
import org.springframework.data.relational.core.sql.In;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productId;
    private Long customerId;
    private Long ownerId;
    private Integer quantity;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
