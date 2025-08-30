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

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;


    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<BookingDto>> reserveItem(@Valid @RequestBody BookingRequest bookingRequest) {
        try{
        BookingDto bookingDto = bookingService.bookItem(bookingRequest);
        ApiResponse<BookingDto> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(bookingDto);
        response.setMessage("Booking " + bookingDto.getStatus());
        return ResponseEntity.ok(response);
        }
        catch(InvalidBookingException e){
            ApiResponse<BookingDto> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingDto>> cancelBooking(@PathVariable Long id) {
        try {
            BookingDto bookingDto = bookingService.cancelBooking(id);
            ApiResponse<BookingDto> response = new ApiResponse<>();
            response.setSuccess(true);
            response.setData(bookingDto);
            response.setMessage("Booking cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (InvalidBookingException e) {
            ApiResponse<BookingDto> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
