package org.example.bookingrent.service;


import org.example.bookingrent.dto.BookingDto;
import org.example.bookingrent.exception.InvalidBookingException;
import org.example.bookingrent.model.Booking;
import org.example.bookingrent.req_res.BookingRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface BookingService {
    BookingDto bookItem(BookingRequest bookingDto) throws InvalidBookingException;
    BookingDto cancelBooking(Long bookingId) throws InvalidBookingException;
    Map<String, List<BookingDto>> getBookingsByUser();
    BookingDto getBooking(Long bookingId) throws InvalidBookingException;
}
