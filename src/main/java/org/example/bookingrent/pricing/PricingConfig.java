package org.example.bookingrent.pricing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class PricingConfig {

    @Bean
    @Profile("discount-pricing")
    public PricingStrategy discountPricingStrategy() {
        return new DiscountPrising();
    }

    @Bean
    @Profile("standard-pricing")
    public PricingStrategy standardPricingStrategy() {
        return new StandardPricing();
    }

    @Bean
    @Primary
    public PricingStrategy defaultPricingStrategy() {
        return new StandardPricing();
    }
}
