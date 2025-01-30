package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;


@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {
    Long id;
    Long itemId;
    Long userId;
    LocalDateTime start;
    LocalDateTime end;
    BookingStatus status;
    String review;
}
