package org.example.bookingrent.req_res;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookingCancelPub {
    private String productId;
    private int quantity;
    private String status;
}
