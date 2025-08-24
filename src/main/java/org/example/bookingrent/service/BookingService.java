package org.example.bookingrent.service;


import org.example.bookingrent.dto.BookingDto;
import org.springframework.stereotype.Service;


public interface BookingService {
    BookingDto bookItem(BookingDto bookingDto);
}
