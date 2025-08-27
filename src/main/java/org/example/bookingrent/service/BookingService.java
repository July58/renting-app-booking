package org.example.bookingrent.service;


import org.example.bookingrent.dto.BookingDto;
import org.example.bookingrent.exception.InvalidBookingException;
import org.example.bookingrent.req_res.BookingRequest;
import org.springframework.stereotype.Service;


public interface BookingService {
    BookingDto bookItem(BookingRequest bookingDto) throws InvalidBookingException;
}
