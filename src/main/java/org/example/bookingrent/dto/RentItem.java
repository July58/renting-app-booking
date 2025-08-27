package org.example.bookingrent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
public class RentItem {

    private String id;
    private String name;
    private String price;

    @JsonProperty("category_id")
    private String categoryId;

    @JsonProperty("amount_of_objects")
    private int amountOfObjects;

    @JsonProperty("current_amount_of_objects")
    private int amountOfCurrent;

    @JsonProperty("user_id")
    private int userId;
}
