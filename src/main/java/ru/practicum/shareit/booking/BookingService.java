package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

public interface BookingService {
    BookingDtoOut createBooking(Long userId, BookingDto bookingDto);

    BookingDtoOut confirmBooking(Long userId, Long bookingId, Boolean approved);

    BookingDtoOut getBooking(Long userId, Long bookingId);

    List<BookingDtoOut> getAllBookings(Long userId, String state);

    List<BookingDtoOut> getAllBookingsForOwner(Long userId, String state);
}
