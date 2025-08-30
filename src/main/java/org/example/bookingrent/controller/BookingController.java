package org.example.bookingrent.controller;

import jakarta.validation.Valid;
import org.example.bookingrent.dto.BookingDto;
import org.example.bookingrent.exception.InvalidBookingException;
import org.example.bookingrent.model.BookingStatus;
import org.example.bookingrent.req_res.ApiResponse;
import org.example.bookingrent.req_res.BookingRequest;
import org.example.bookingrent.service.BookingService;
import org.example.bookingrent.service.impl.BookingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ApiResponse<BookingDto> reserveItem(@Valid @RequestBody BookingRequest bookingRequest) throws InvalidBookingException {
        BookingDto bookingDto = bookingService.bookItem(bookingRequest);
        if(bookingDto.getStatus().equals(BookingStatus.FAILED.toString())) {
            throw new InvalidBookingException("Booking failed due to unavailability or other issues.");
        }
        ApiResponse<BookingDto> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(bookingDto);
        response.setMessage("Booking " + bookingDto.getStatus());
        return response;
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<BookingDto> cancelBooking(@PathVariable Long id) throws InvalidBookingException {
        BookingDto bookingDto = bookingService.cancelBooking(id);
        if(bookingDto.getStatus().equals(BookingStatus.FAILED.toString())) {
            throw new InvalidBookingException("Booking failed due to unavailability or other issues.");
        }
        ApiResponse<BookingDto> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(bookingDto);
        response.setMessage("Booking cancelled successfully");
        return response;
    }

    @GetMapping
    public ApiResponse<Map<String, List<BookingDto>>> getBookings() {
        ApiResponse<Map<String, List<BookingDto>>> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(bookingService.getBookingsByUser());
        response.setMessage("Fetched bookings successfully");
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingDto> getBooking(@PathVariable Long id) throws InvalidBookingException {
        BookingDto bookingDto = bookingService.getBooking(id);
        ApiResponse<BookingDto> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(bookingDto);
        response.setMessage("Fetched booking successfully");
        return response;
    }
}

