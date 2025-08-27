package org.example.bookingrent.req_res;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Quantity is required")
    @Pattern(regexp = "\\d+", message = "Quantity must be a positive integer")
    @Min(value = 1, message = "Quantity must be at least 1")
    private String quantity;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate fromDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate toDate;
}
