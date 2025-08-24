package org.example.bookingrent.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingDto {
    private String productId;
    private String customerId;
    private String ownerId;
    private int quantity;
    private BigDecimal price;
    private String status;
    private LocalDate fromDate;
    private LocalDate toDate;
}
