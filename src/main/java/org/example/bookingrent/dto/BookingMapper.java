package org.example.bookingrent.dto;

import org.example.bookingrent.model.Booking;
import org.example.bookingrent.model.BookingStatus;

import java.math.BigDecimal;

public class BookingMapper {
    public static Booking toEntity(BookingDto dto) {
        Booking booking = new Booking();
        booking.setProductId(dto.getProductId());
        booking.setCustomerId(Integer.parseInt(dto.getCustomerId()));
        booking.setOwnerId(Integer.parseInt(dto.getOwnerId()));
        booking.setQuantity(dto.getQuantity());
        booking.setPrice(dto.getPrice());
        booking.setFromDate(dto.getFromDate());
        booking.setToDate(dto.getToDate());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(java.time.LocalDateTime.now());
        return booking;
    }
}
