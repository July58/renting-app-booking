package org.example.bookingrent.service.impl;


import com.fasterxml.jackson.databind.JsonNode;
import org.example.bookingrent.dto.BookingDto;
import org.example.bookingrent.dto.BookingMapper;
import org.example.bookingrent.dto.RentItem;
import org.example.bookingrent.model.Booking;
import org.example.bookingrent.model.BookingStatus;
import org.example.bookingrent.pricing.DiscountPrising;
import org.example.bookingrent.pricing.PricingStrategy;
import org.example.bookingrent.repository.BookingRepository;
import org.example.bookingrent.service.BookingService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class BookingServiceImpl implements BookingService {

    private final ExternalServiceClient externalServiceClient;
    private final BookingRepository bookingRepository;
    private final PricingStrategy pricingStrategy;

    public BookingServiceImpl(ExternalServiceClient externalServiceClient,
                          BookingRepository bookingRepository,
                          PricingStrategy pricingStrategy) {
        this.externalServiceClient = externalServiceClient;
        this.bookingRepository = bookingRepository;
        this.pricingStrategy = new DiscountPrising();
    }

    @Override
    public BookingDto bookItem(BookingDto bookingDto) {
        setCustomerFromAuth(bookingDto);

        RentItem rentItem = checkItemAvailability(bookingDto);
        if (rentItem == null) {
            bookingDto.setStatus(BookingStatus.FAILED.toString());
            return bookingDto;
        }

        BigDecimal finalPrice = calculateFinalPrice(bookingDto, rentItem);
        bookingDto.setPrice(finalPrice);

        boolean reserved = reserveItem(rentItem, bookingDto.getQuantity());
        if (!reserved) {
            bookingDto.setStatus(BookingStatus.FAILED.toString());
            return bookingDto;
        }

        bookingDto.setOwnerId(String.valueOf(rentItem.getUserId()));

        saveBooking(bookingDto);

        bookingDto.setStatus(BookingStatus.CONFIRMED.toString());
        return bookingDto;
    }

    private void setCustomerFromAuth(BookingDto bookingDto) {
        JsonNode userData = (JsonNode) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String customerId = userData.path("id").asText();
        bookingDto.setCustomerId(customerId);
    }

    private RentItem checkItemAvailability(BookingDto bookingDto) {
        return externalServiceClient.checkAvailability(
                bookingDto.getProductId(),
                bookingDto.getQuantity()
        );
    }

    private BigDecimal calculateFinalPrice(BookingDto bookingDto, RentItem rentItem) {
        BigDecimal unitPrice;
        try {
            unitPrice = new BigDecimal(rentItem.getPrice());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price format: " + rentItem.getPrice());
        }
        return pricingStrategy.calculatePrice(
                bookingDto.getQuantity(),
                unitPrice,
                bookingDto.getFromDate(),
                bookingDto.getToDate()
        );
    }

    private boolean reserveItem(RentItem rentItem, int quantity) {
        return externalServiceClient.reserveItem(rentItem.getId(), quantity);
    }

    private void saveBooking(BookingDto bookingDto) {
        Booking booking = BookingMapper.toEntity(bookingDto);
        bookingRepository.save(booking);
    }


}

