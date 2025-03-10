package ru.practicum.shareit;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingServiceImpl;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    void confirmBooking_success() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDtoOut result = bookingService.confirmBooking(owner.getId(), booking.getId(), true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void confirmBooking_whenNotOwner_thenThrowException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.confirmBooking(user.getId(), booking.getId(), true));
    }
}
