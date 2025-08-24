package org.example.bookingrent.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class StandardPricing implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(int quantity, BigDecimal unitPrice, LocalDate fromDate, LocalDate toDate) {

        long days = ChronoUnit.DAYS.between(fromDate, toDate) + 1;

        return unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.valueOf(days));
    }
}
