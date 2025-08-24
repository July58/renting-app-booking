package org.example.bookingrent.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PricingStrategy {
    BigDecimal calculatePrice(int quantity, BigDecimal unitPrice, LocalDate fromDate, LocalDate toDate);
}
