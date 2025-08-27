package org.example.bookingrent.exception;

public class InvalidBookingException extends Exception{

    public InvalidBookingException() {
        super();
    }

    public InvalidBookingException(String message) {
        super(message);
    }

    public InvalidBookingException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidBookingException(Throwable cause) {
        super(cause);
    }
}
