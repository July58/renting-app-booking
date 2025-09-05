package org.example.bookingrent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import org.example.bookingrent.dto.BookingDto;
import org.example.bookingrent.exception.InvalidBookingException;
import org.example.bookingrent.model.BookingStatus;
import org.example.bookingrent.req_res.ApiResponse;
import org.example.bookingrent.req_res.BookingRequest;
import org.example.bookingrent.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
@Tag(name = "Booking Controller", description = "APIs for booking, canceling, and retrieving bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    @Operation(
            summary = "Reserve an item",
            description = "Create a booking for an item. Throws InvalidBookingException if booking fails.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Booking created successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid booking request",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    public ApiResponse<BookingDto> reserveItem(@Valid @RequestBody BookingRequest bookingRequest) throws InvalidBookingException {
        BookingDto bookingDto = bookingService.bookItem(bookingRequest);
        if (bookingDto.getStatus().equals(BookingStatus.FAILED.toString())) {
            throw new InvalidBookingException("Booking failed due to unavailability or other issues.");
        }
        ApiResponse<BookingDto> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(bookingDto);
        response.setMessage("Booking " + bookingDto.getStatus());
        return response;
    }

    @PutMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel a booking",
            description = "Cancel an existing booking by its ID. Throws InvalidBookingException if cancellation fails.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Booking cancelled successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Cancellation failed",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    public ApiResponse<BookingDto> cancelBooking(@PathVariable Long id) throws InvalidBookingException {
        BookingDto bookingDto = bookingService.cancelBooking(id);
        if (bookingDto.getStatus().equals(BookingStatus.FAILED.toString())) {
            throw new InvalidBookingException("Booking failed due to unavailability or other issues.");
        }
        ApiResponse<BookingDto> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(bookingDto);
        response.setMessage("Booking cancelled successfully");
        return response;
    }

    @GetMapping
    @Operation(
            summary = "Get all bookings for the current user",
            description = "Retrieve all bookings grouped by status or other criteria.",

            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Fetched bookings successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    public ApiResponse<Map<String, List<BookingDto>>> getBookings() {
        ApiResponse<Map<String, List<BookingDto>>> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(bookingService.getBookingsByUser());
        response.setMessage("Fetched bookings successfully");
        return response;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get booking by ID",
            description = "Retrieve a single booking by its ID. Throws InvalidBookingException if not found.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Fetched booking successfully",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Booking not found",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    public ApiResponse<BookingDto> getBooking(@PathVariable Long id) throws InvalidBookingException {
        BookingDto bookingDto = bookingService.getBooking(id);
        ApiResponse<BookingDto> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(bookingDto);
        response.setMessage("Fetched booking successfully");
        return response;
    }
}
