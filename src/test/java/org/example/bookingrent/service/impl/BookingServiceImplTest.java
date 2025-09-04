package org.example.bookingrent.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.bookingrent.dto.*;
import org.example.bookingrent.exception.InvalidBookingException;
import org.example.bookingrent.model.*;
import org.example.bookingrent.pricing.PricingStrategy;
import org.example.bookingrent.repository.BookingRepository;
import org.example.bookingrent.req_res.BookingCancelPub;
import org.example.bookingrent.req_res.BookingRequest;
import org.example.bookingrent.service.ExternalServiceClient;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    @Mock
    private ExternalServiceClient externalServiceClient;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PricingStrategy pricingStrategy;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode userNode = mapper.createObjectNode();
        userNode.put("id", "123");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userNode);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void bookItem_successfulBooking() throws InvalidBookingException {
        BookingRequest request = new BookingRequest();
        request.setProductId("1");
        request.setQuantity("2");

        RentItem rentItem = new RentItem();
        rentItem.setId("1");
        rentItem.setPrice("100");
        rentItem.setUserId(10);

        when(externalServiceClient.checkAvailability(anyString(), anyInt())).thenReturn(rentItem);
        when(pricingStrategy.calculatePrice(anyInt(), any(), any(), any())).thenReturn(BigDecimal.valueOf(200));
        when(externalServiceClient.reserveItem(any(), anyInt())).thenReturn(true);

        BookingDto result = bookingService.bookItem(request);

        assertEquals(BookingStatus.CONFIRMED.toString(), result.getStatus());
        assertEquals(BigDecimal.valueOf(200), result.getPrice());
    }

    @Test
    void bookItem_itemUnavailable() {
        BookingRequest request = new BookingRequest();
        request.setProductId("1");
        request.setQuantity("2");

        when(externalServiceClient.checkAvailability(anyString(), anyInt())).thenReturn(null);

        assertThrows(InvalidBookingException.class, () -> bookingService.bookItem(request));
    }

    @Test
    void bookItem_priceCalculationError() throws InvalidBookingException {
        BookingRequest request = new BookingRequest();
        request.setProductId("1");
        request.setQuantity("2");

        RentItem rentItem = new RentItem();
        rentItem.setId("1");
        rentItem.setPrice("bad_price");
        rentItem.setUserId(10);

        when(externalServiceClient.checkAvailability(anyString(), anyInt())).thenReturn(rentItem);

        BookingDto result = bookingService.bookItem(request);

        assertEquals(BookingStatus.FAILED.toString(), result.getStatus());
    }

    @Test
    void bookItem_reservationFails() throws InvalidBookingException {
        BookingRequest request = new BookingRequest();
        request.setProductId("1");
        request.setQuantity("2");

        RentItem rentItem = new RentItem();
        rentItem.setId("1");
        rentItem.setPrice("100");
        rentItem.setUserId(10);

        when(externalServiceClient.checkAvailability(anyString(), anyInt())).thenReturn(rentItem);
        when(pricingStrategy.calculatePrice(anyInt(), any(), any(), any())).thenReturn(BigDecimal.valueOf(200));
        when(externalServiceClient.reserveItem(any(), anyInt())).thenReturn(false);

        assertThrows(InvalidBookingException.class, () -> bookingService.bookItem(request));

    }

    @Test
    void cancelBooking_success() throws InvalidBookingException {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setProductId("1");
        booking.setQuantity(2);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.cancelBooking(1L);

        assertEquals(BookingStatus.CANCELLED.toString(), result.getStatus());
        verify(bookingRepository).save(any());
        verify(externalServiceClient).publishCompletedBooking(any(BookingCancelPub.class));
    }

    @Test
    void cancelBooking_notFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> bookingService.cancelBooking(1L));
    }

    @Test
    void cancelBooking_wrongStatus() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        assertThrows(InvalidBookingException.class, () -> bookingService.cancelBooking(1L));
    }


    @Test
    void completeBookings_shouldCompleteAndPublishConfirmedBookings() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setProductId("1");
        booking.setQuantity(2);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setToDate(LocalDate.now().minusDays(1));

        List<Booking> toComplete = List.of(booking);

        when(bookingRepository.findByStatusAndToDateBefore(eq(BookingStatus.CONFIRMED), any(LocalDate.class)))
                .thenReturn(toComplete);

        bookingService.completeBookings();

        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
        verify(externalServiceClient).publishCompletedBooking(any(BookingCancelPub.class));
        verify(bookingRepository).save(booking);
    }

    @Test
    void completeBookings_shouldDoNothingIfNoBookingsToComplete() {
        when(bookingRepository.findByStatusAndToDateBefore(eq(BookingStatus.CONFIRMED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        bookingService.completeBookings();

        verify(externalServiceClient, never()).publishCompletedBooking(any());
        verify(bookingRepository, never()).save(any());
    }
}