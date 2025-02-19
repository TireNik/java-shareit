package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;


    @PostMapping
    public ResponseEntity<BookingDtoOut> createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestBody BookingDto bookingDto) {
        log.info("Создание бронирования: {}", bookingDto);
        BookingDtoOut createdBooking = bookingService.createBooking(userId, bookingDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDtoOut> confirmBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {
        log.info("Подтверждение/Отклонение бронирования: bookingId={}, approved={}", bookingId, approved);
        BookingDtoOut updatedBooking = bookingService.confirmBooking(userId, bookingId, approved);
        return ResponseEntity.ok(updatedBooking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDtoOut> getBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId) {
        log.info("Получение информации о бронировании: bookingId={}", bookingId);
        BookingDtoOut booking = bookingService.getBooking(userId, bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<BookingDtoOut>> getAllBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получение списка бронирований для пользователя: userId={}, state={}", userId, state);
        List<BookingDtoOut> bookings = bookingService.getAllBookings(userId, state);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDtoOut>> getAllBookingsForOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получение списка бронирований для владельца: userId={}, state={}", userId, state);
        List<BookingDtoOut> bookings = bookingService.getAllBookingsForOwner(userId, state);
        return ResponseEntity.ok(bookings);
    }
}