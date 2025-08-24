package org.example.bookingrent.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class DiscountPrising implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(int quantity, BigDecimal unitPrice, LocalDate fromDate, LocalDate toDate) {

        long days = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.valueOf(days));

        if (quantity > 3) {
            total = total.multiply(new BigDecimal("0.9"));
        }

        return total;
    }
}
