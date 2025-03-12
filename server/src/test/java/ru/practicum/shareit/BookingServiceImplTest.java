package ru.practicum.shareit;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingServiceImpl;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User", "user@email.com");
        owner = new User(2L, "Owner", "owner@email.com");
        item = new Item(1L, "Item", "Description", true, owner, null);
        bookingDto = new BookingDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        booking = new Booking(1L, item, user, bookingDto.getStart(), bookingDto.getEnd(), BookingStatus.WAITING);
    }

    @Test
    void createBooking_success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDtoOut result = bookingService.createBooking(user.getId(), bookingDto);

        assertNotNull(result);
        assertEquals(bookingDto.getStart(), result.getStart());
        assertEquals(bookingDto.getEnd(), result.getEnd());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void createBooking_whenItemUnavailable_thenThrowException() {
        item.setAvailable(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(user.getId(), bookingDto));
    }

    @Test
    void createBooking_whenBookingOwnItem_thenThrowException() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(owner.getId(), bookingDto));
    }

    @Test
    void confirmBooking_success() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDtoOut result = bookingService.confirmBooking(owner.getId(), booking.getId(), true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void confirmBooking_whenAlreadyApproved_thenThrowException() {
        booking.setStatus(BookingStatus.APPROVED);

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.confirmBooking(owner.getId(), booking.getId(), true));

        assertEquals("Бронирование уже подтверждено или отклонено", exception.getMessage());
    }

    @Test
    void getBooking_success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingDtoOut result = bookingService.getBooking(user.getId(), booking.getId());

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void getBooking_whenNotBookerOrOwner_thenThrowException() {
        User anotherUser = new User(3L, "Another User", "another@email.com");

        when(userRepository.findById(anotherUser.getId())).thenReturn(Optional.of(anotherUser));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.getBooking(anotherUser.getId(), booking.getId()));
    }

    @Test
    void getAllBookings_success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerId(user.getId(), Sort.by(Sort.Order.desc("start"))))
                .thenReturn(Collections.emptyList());

        List<BookingDtoOut> result = bookingService.getAllBookings(user.getId(), "ALL");

        assertTrue(result.isEmpty(), "Список бронирований должен быть пустым");
    }

    @Test
    void getAllBookingsForOwner_success() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerId(owner.getId(), Sort.by(Sort.Order.desc("start"))))
                .thenReturn(Collections.emptyList());

        List<BookingDtoOut> result = bookingService.getAllBookingsForOwner(owner.getId(), "ALL");

        assertTrue(result.isEmpty(), "Список бронирований владельца должен быть пустым");
    }

    @Test
    void createBooking_whenEndBeforeStart_thenThrowException() {
        bookingDto = new BookingDto(item.getId(), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        assertThrows(NullPointerException.class, () -> bookingService.createBooking(user.getId(), bookingDto));
    }

    @Test
    void createBooking_whenStartEqualsEnd_thenThrowException() {
        LocalDateTime now = LocalDateTime.now().plusDays(1);
        bookingDto = new BookingDto(item.getId(), now, now);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        assertThrows(NullPointerException.class, () -> bookingService.createBooking(user.getId(), bookingDto));
    }

    @Test
    void createBooking_whenUserNotFound_thenThrowException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> bookingService.createBooking(user.getId(), bookingDto));
    }

    @Test
    void getAllBookings_withDifferentStates() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        for (String state : List.of("CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED")) {
            when(bookingRepository.findByBookerIdAndStatus(user.getId(), BookingStatus.valueOf(state), Sort.by(Sort.Order.desc("start"))))
                    .thenReturn(Collections.emptyList());
            List<BookingDtoOut> result = bookingService.getAllBookings(user.getId(), state);
            assertTrue(result.isEmpty(), "Список бронирований должен быть пустым для состояния " + state);
        }
    }

    @Test
    void confirmBooking_whenApprovedNull_thenThrowException() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        assertThrows(NullPointerException.class, () -> bookingService.confirmBooking(owner.getId(), booking.getId(), null));
    }

    @Test
    void confirmBooking_whenNotOwner_thenThrowException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        assertThrows(ValidationException.class, () -> bookingService.confirmBooking(user.getId(), booking.getId(), true));
    }
}