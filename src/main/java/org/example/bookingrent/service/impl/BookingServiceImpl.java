package org.example.bookingrent.service.impl;


import com.fasterxml.jackson.databind.JsonNode;
import org.example.bookingrent.dto.BookingDto;
import org.example.bookingrent.dto.BookingMapper;
import org.example.bookingrent.dto.RentItem;
import org.example.bookingrent.exception.InvalidBookingException;
import org.example.bookingrent.model.Booking;
import org.example.bookingrent.model.BookingStatus;
import org.example.bookingrent.pricing.PricingStrategy;
import org.example.bookingrent.repository.BookingRepository;
import org.example.bookingrent.req_res.BookingCancelPub;
import org.example.bookingrent.req_res.BookingRequest;
import org.example.bookingrent.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class BookingServiceImpl implements BookingService {

    private final ExternalServiceClient externalServiceClient;
    private final BookingRepository bookingRepository;
    private final PricingStrategy pricingStrategy;
    private static final Logger logger = LoggerFactory.getLogger("GLOBAL_LOGGER");

    public BookingServiceImpl(ExternalServiceClient externalServiceClient,
                              BookingRepository bookingRepository,
                              PricingStrategy pricingStrategy) {
        this.externalServiceClient = externalServiceClient;
        this.bookingRepository = bookingRepository;
        this.pricingStrategy = pricingStrategy;
    }

    @Override
    public BookingDto bookItem(BookingRequest bookingRequest) throws InvalidBookingException {
        logger.info("Received booking request for productId={}, quantity={}",
                bookingRequest.getProductId(), bookingRequest.getQuantity());

        BookingDto bookingDto = BookingMapper.toDtoFromRequest(bookingRequest);

        setCustomerFromAuth(bookingDto);
        logger.debug("BookingDto after setting customer: {}", bookingDto);

        RentItem rentItem = checkItemAvailability(bookingDto);
        if (rentItem == null) {
            bookingDto.setStatus(BookingStatus.FAILED.toString());
            logger.warn("Item not available for productId={} and quantity={}",
                    bookingDto.getProductId(), bookingDto.getQuantity());
            throw new InvalidBookingException("Item not available: booked quantity exceeds available stock");
        }


        BigDecimal finalPrice;
        try {
            finalPrice = calculateFinalPrice(bookingDto, rentItem);
            bookingDto.setPrice(finalPrice);
            logger.debug("Calculated final price: {}", finalPrice);
        } catch (IllegalArgumentException e) {
            bookingDto.setStatus(BookingStatus.FAILED.toString());
            logger.error("Error calculating price for productId={} : {}", bookingDto.getProductId(), e.getMessage());
            return bookingDto;
        }

        boolean reserved = reserveItem(rentItem, bookingDto.getQuantity());
        if (!reserved) {
            bookingDto.setStatus(BookingStatus.FAILED.toString());
            logger.warn("Failed to reserve item for productId={} quantity={}",
                    bookingDto.getProductId(), bookingDto.getQuantity());
            return bookingDto;
        }

        bookingDto.setOwnerId(String.valueOf(rentItem.getUserId()));
        logger.trace("Owner set for booking: {}", bookingDto.getOwnerId());

        try {
            saveBooking(bookingDto);
            logger.info("Booking saved successfully: {}", bookingDto);
        } catch (Exception e) {
            bookingDto.setStatus(BookingStatus.FAILED.toString());
            logger.error("Error saving booking for productId={} : {}", bookingDto.getProductId(), e.getMessage(), e);
            return bookingDto;
        }

        bookingDto.setStatus(BookingStatus.CONFIRMED.toString());
        logger.info("Booking confirmed for productId={} customerId={}",
                bookingDto.getProductId(), bookingDto.getCustomerId());

        return bookingDto;
    }

    @Override
    public BookingDto cancelBooking(Long bookingId) throws InvalidBookingException {
        if(bookingId == null || bookingId <= 0) {
            logger.warn("Invalid bookingId provided for cancellation: {}", bookingId);
            throw new InvalidBookingException("Invalid booking ID");
        }
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            logger.warn("Booking not found for cancellation with bookingId={}", bookingId);
            throw new InvalidBookingException("Booking not found");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            logger.warn("Cannot cancel booking with status={} for bookingId={}", booking.getStatus(), bookingId);
            throw new InvalidBookingException("Only confirmed bookings can be cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        logger.info("Booking cancelled successfully for bookingId={}", bookingId);
        BookingCancelPub bookingCancelPub = new BookingCancelPub();
        bookingCancelPub.setProductId(booking.getProductId());
        bookingCancelPub.setQuantity(booking.getQuantity());
        bookingCancelPub.setStatus(booking.getStatus().toString());
        externalServiceClient.publishCompletedBooking(bookingCancelPub);
        logger.info("Booking updated info was sent for bookingId={}", bookingId);
        return BookingMapper.toDto(booking);
    }

    @Scheduled(fixedRate = 60000)
    public void completeBookings() {
        LocalDate now = LocalDate.now();
        List<Booking> toComplete = bookingRepository.findByStatusAndToDateBefore(BookingStatus.CONFIRMED, now);
        if(!toComplete.isEmpty()) {
        for (Booking booking : toComplete) {
            BookingCancelPub bookingCancelPub = new BookingCancelPub();
            bookingCancelPub.setProductId(booking.getProductId());
            bookingCancelPub.setQuantity(booking.getQuantity());
            bookingCancelPub.setStatus(booking.getStatus().toString());
            externalServiceClient.publishCompletedBooking(bookingCancelPub);
            logger.info("Published completed booking event for bookingId={}", booking.getId());
            booking.setUpdatedAt(LocalDateTime.now());
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
            logger.info("Booking updated to complete successfully: {}", booking);
        }}
    }

    private void setCustomerFromAuth(BookingDto bookingDto) {
        JsonNode userData = (JsonNode) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String customerId = userData.path("id").asText();
        bookingDto.setCustomerId(customerId);
        logger.trace("Customer set from authentication: {}", customerId);
    }

    private RentItem checkItemAvailability(BookingDto bookingDto) throws InvalidBookingException {
        RentItem item = null;
        try {
           item = externalServiceClient.checkAvailability(
                    bookingDto.getProductId(),
                    bookingDto.getQuantity()
            );
            if (item != null) {
                logger.debug("Item available: {}", item);
            }

        }catch (io.dapr.exceptions.DaprException e) {
            logger.error("Product not found in external service: {}", bookingDto.getProductId(), e);
            throw new InvalidBookingException("Product not found: " + bookingDto.getProductId());
        }

        return item;
    }

    private BigDecimal calculateFinalPrice(BookingDto bookingDto, RentItem rentItem) {
        BigDecimal unitPrice;
        try {
            unitPrice = new BigDecimal(rentItem.getPrice());
        } catch (NumberFormatException e) {
            logger.error("Invalid price format for productId={}: {}", bookingDto.getProductId(), rentItem.getPrice());
            throw new IllegalArgumentException("Invalid price format: " + rentItem.getPrice());
        }
        BigDecimal price = pricingStrategy.calculatePrice(
                bookingDto.getQuantity(),
                unitPrice,
                bookingDto.getFromDate(),
                bookingDto.getToDate()
        );
        logger.debug("Price calculated using pricing strategy: {}", price);
        return price;
    }

    private boolean reserveItem(RentItem rentItem, int quantity) {
        boolean success = externalServiceClient.reserveItem(rentItem, quantity);
        logger.debug("Reserve item result for productId={} quantity={}: {}",
                rentItem.getId(), quantity, success);
        return success;
    }

    private void saveBooking(BookingDto bookingDto) {
        Booking booking = BookingMapper.toEntity(bookingDto);
        bookingRepository.save(booking);
        logger.debug("Booking entity saved: {}", booking);
    }
}


