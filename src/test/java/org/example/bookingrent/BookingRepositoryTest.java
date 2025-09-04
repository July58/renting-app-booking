package org.example.bookingrent;


import io.dapr.client.DaprClient;
import org.example.bookingrent.model.Booking;
import org.example.bookingrent.model.BookingStatus;
import org.example.bookingrent.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;


import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
    }


    @Test
    void testSaveAndFindByStatusAndToDateBefore() {
        Booking booking = new Booking();
        booking.setProductId("1");
        booking.setQuantity(2);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setToDate(LocalDate.now().minusDays(2));
        booking.setFromDate(LocalDate.now().minusDays(5));
        booking.setCustomerId(123L);

        bookingRepository.save(booking);

        List<Booking> found = bookingRepository.findByStatusAndToDateBefore(
                BookingStatus.CONFIRMED, LocalDate.now());

        assertEquals(1, found.size());
        assertEquals("1", found.get(0).getProductId());
    }

    @Test
    void testFindByCustomerId() {
        Booking booking = new Booking();
        booking.setProductId("2");
        booking.setQuantity(1);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setToDate(LocalDate.now().plusDays(1));
        booking.setFromDate(LocalDate.now());
        booking.setCustomerId(456L);

        bookingRepository.save(booking);

        List<Booking> found = bookingRepository.findByCustomerId(456L);

        assertEquals(1, found.size());
        assertEquals("2", found.get(0).getProductId());
    }
}