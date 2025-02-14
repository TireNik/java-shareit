package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

public interface BookingService {
    BookingDtoOut createBooking(Long userId, BookingDto bookingDto);
    BookingDtoOut confirmBooking(Long userId, Long bookingId, Boolean approved);
    BookingDtoOut getBooking(Long userId, Long bookingId);
}
