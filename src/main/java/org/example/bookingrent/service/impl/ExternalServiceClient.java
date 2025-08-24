package org.example.bookingrent.service.impl;


import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.bookingrent.dto.RentItem;
import org.springframework.stereotype.Service;


@Service
public class ExternalServiceClient {

    private final DaprClient daprClient;

    public ExternalServiceClient(DaprClient daprClient) {
        this.daprClient = daprClient;
    }

    /**
     * Check availability of an item.
     */
    @CircuitBreaker(name = "catalogServiceCB", fallbackMethod = "checkAvailabilityFallback")
    public RentItem checkAvailability(String itemId, int requestedAmount) {

        return daprClient.invokeMethod(
                "rentable-service",
                "api/rent/" + itemId,
                null,
                HttpExtension.GET,
                RentItem.class
        ).block();
    }

    public boolean checkAvailabilityFallback(String itemId, int requestedAmount, Throwable throwable) {
        System.err.println("CatalogService unavailable (availability check): " + throwable.getMessage());
        return false;
    }

    /**
     * Reserve item by updating amount_of_objects.
     */
    @CircuitBreaker(name = "catalogServiceCB", fallbackMethod = "reserveItemFallback")
    public boolean reserveItem(String itemId, int quantity) {

        RentItem item = daprClient.invokeMethod(
                "rentable-service",
                "api/rent/" + itemId,
                null,
                HttpExtension.PUT,
                RentItem.class
        ).block();

        if (item == null || item.getAmountOfCurrent() < quantity) {
            return false;
        }


        item.setAmountOfCurrent(item.getAmountOfCurrent() - quantity);

        RentItem updated = daprClient.invokeMethod(
                "catalog-service",
                "api/rent/" + itemId,
                item,
                HttpExtension.PUT,
                RentItem.class
        ).block();

        return updated != null;
    }

    public boolean reserveItemFallback(String itemId, int quantity, Throwable throwable) {
        System.err.println("CatalogService unavailable (reserveItem): " + throwable.getMessage());
        return false;
    }


}
