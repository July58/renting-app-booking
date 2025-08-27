package org.example.bookingrent.repository;


import org.example.bookingrent.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.productId = :itemId " +
            "AND (b.fromDate <= :endDate AND b.toDate >= :startDate)"+
            "AND b.status = 'CONFIRMED'")
    List<Booking> findOverlappingBookings(String itemId, LocalDate startDate, LocalDate endDate);
}

