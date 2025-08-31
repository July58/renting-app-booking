package org.example.bookingrent.service.impl;


import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.dapr.utils.TypeRef;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.bookingrent.dto.BookingDto;
import org.example.bookingrent.dto.RentItem;
import org.example.bookingrent.model.Booking;
import org.example.bookingrent.req_res.ApiResponse;
import org.example.bookingrent.req_res.BookingCancelPub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class ExternalServiceClient {

    private final DaprClient daprClient;
    private static final Logger logger = LoggerFactory.getLogger(ExternalServiceClient.class);

    public ExternalServiceClient(DaprClient daprClient) {
        this.daprClient = daprClient;
    }

    /**
     * Check availability of an item.
     */
    @CircuitBreaker(name = "rentable-service", fallbackMethod = "checkAvailabilityFallback")
    public RentItem checkAvailability(String itemId, int requestedAmount) {

        logger.debug("reserveItem called with itemId: {}, quantity: {}", itemId, requestedAmount);

        ApiResponse<RentItem> item = daprClient.invokeMethod(
                "rentable-service",
                "api/rent/" + itemId,
                null,
                HttpExtension.GET,
                getAuthHeadersFromPrincipal(),
                new TypeRef<ApiResponse<RentItem>>() {}
        ).block();

        logger.info("Fetched item: {}", item);

        if (item == null || item.getData().getAmountOfCurrent() < requestedAmount) {
            logger.warn("Item is null or not enough quantity. Returning false");
            return null;
        }

        return item.getData();
    }

    public RentItem checkAvailabilityFallback(String itemId, int requestedAmount, Throwable throwable) {
        logger.error("CatalogService unavailable (availability check): {}", throwable.getMessage());
        return null;
    }

    /**
     * Reserve item by updating amount_of_objects.
     */
    @CircuitBreaker(name = "rentable-service", fallbackMethod = "reserveItemFallback")
    public boolean reserveItem(RentItem item, int quantity) {

        item.setAmountOfCurrent(item.getAmountOfCurrent() - quantity);

        RentItem updated = daprClient.invokeMethod(
                "rentable-service",
                "api/rent/" + item.getId(),
                item,
                HttpExtension.PUT,
                getAuthHeadersFromPrincipal(),
                RentItem.class
        ).block();

        logger.info("Updated item: {}", updated);

        return updated != null;
    }

    public boolean reserveItemFallback(RentItem item, int quantity, Throwable throwable) {
        logger.error("CatalogService unavailable (reserveItem): {}", throwable.getMessage());
        return false;
    }

    private Map<String, String> getAuthHeadersFromPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, String> headers = new HashMap<>();

        if (authentication != null && authentication.getCredentials() != null) {
            String token = authentication.getCredentials().toString();
            logger.debug("Extracted token from principal: {}", token);
            headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        } else {
            logger.warn("No authentication or credentials found");
        }

        return headers;
    }

    public void publishCompletedBooking(BookingCancelPub booking) {
        daprClient.publishEvent("pubsub", "completed-bookings", booking).block();
    }
}
