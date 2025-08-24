package org.example.bookingrent.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Booking {
    @Id
    private long id;
    private String productId;
    private int customerId;
    private int ownerId;
    private int quantity;
    private BigDecimal price;
    private BookingStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
