package org.example.bookingrent.dto;

import lombok.Data;

@Data
public class RentItem {
    private String id;
    private String name;
    private String price;
    private String categoryId;
    private int amountOfObjects;
    private int amountOfCurrent;
    private int userId;
    private String updatedAt;
    private String createdAt;
}
