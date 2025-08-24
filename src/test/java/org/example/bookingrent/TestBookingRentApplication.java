package org.example.bookingrent;

import org.springframework.boot.SpringApplication;

public class TestBookingRentApplication {

    public static void main(String[] args) {
        SpringApplication.from(BookingRentApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
