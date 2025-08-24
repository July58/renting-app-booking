package org.example.bookingrent.controller;

import org.example.bookingrent.dto.BookingDto;
import org.example.bookingrent.req_res.BookingRequest;
import org.example.bookingrent.service.BookingService;
import org.example.bookingrent.service.impl.BookingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;


    @PostMapping("/reserve")
    public ResponseEntity<BookingDto> reserveItem(@RequestBody BookingRequest bookingRequest) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setProductId(bookingRequest.getProductId());
        bookingDto.setQuantity(Integer.parseInt(bookingRequest.getQuantity()));
        bookingDto.setFromDate(bookingRequest.getFromDate());
        bookingDto.setToDate(bookingRequest.getToDate());
        BookingDto result = bookingService.bookItem(bookingDto);
        if (result.getStatus().equals("FAILED")) {
            return ResponseEntity.badRequest().body(bookingDto);
        }
        return ResponseEntity.ok(result);
    }
}
