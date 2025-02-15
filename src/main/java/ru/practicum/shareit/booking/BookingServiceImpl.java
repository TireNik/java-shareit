package ru.practicum.shareit.booking;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final UserService userService;


    @Override
    @Transactional
    public BookingDtoOut createBooking(Long userId, BookingDto bookingDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        bookingValid(bookingDto, userId, item);

        Booking booking = BookingMapper.toEntity(bookingDto, item, user);
        booking = bookingRepository.save(booking);
        log.info("ERROR {}", booking);

        return BookingMapper.toBookingOut(booking);
    }

    private void bookingValid (BookingDto bookingDto, Long userId, Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("Бронирование вещи не доступно");
        }
        if (userId.equals(item.getOwner().getId())) {
            throw new NotFoundException("Нет доступа к вещи");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ValidationException("Дата окончания бронирования не может быть раньше начала или равна ему");
        }
    }

    @Override
    public BookingDtoOut confirmBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Только владелец может подтвердить или отклонить бронирование");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        return BookingMapper.toBookingOut(booking);
    }

    @Override
    public BookingDtoOut getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование с id " + bookingId + " не найдено"));

        if (!booking.getUser().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("У вас нет прав для просмотра этого бронирования");
        }

        return BookingMapper.toBookingOut(booking);
    }

    @Override
    public List<BookingDtoOut> getAllBookings(Long userId, String state) {
        BookingStatus status = getBookingStatusFromState(state);

        List<Booking> bookings;

        if (status == null) {
            bookings = bookingRepository.findByUserId(userId, Sort.by(Sort.Order.desc("start")));
        } else {
            bookings = bookingRepository.findByUserIdAndStatus(userId, status, Sort.by(Sort.Order.desc("start")));
        }

        return bookings.stream()
                .map(BookingMapper::toBookingOut)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDtoOut> getAllBookingsForOwner(Long userId, String state) {
        userService.getUserById(userId);

        BookingStatus status = getBookingStatusFromState(state);

        List<Booking> bookings;

        if (status == null) {
            bookings = bookingRepository.findByItemOwnerId(userId, Sort.by(Sort.Order.desc("start")));
        } else {
            bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, status, Sort.by(Sort.Order.desc("start")));
        }

        return bookings.stream()
                .map(BookingMapper::toBookingOut)
                .collect(Collectors.toList());
    }

    private BookingStatus getBookingStatusFromState(String state) {
        return switch (state.toUpperCase()) {
            case "CURRENT" -> BookingStatus.CURRENT;
            case "PAST" -> BookingStatus.PAST;
            case "FUTURE" -> BookingStatus.FUTURE;
            case "WAITING" -> BookingStatus.WAITING;
            case "REJECTED" -> BookingStatus.REJECTED;
            default -> null;
        };
    }
}
