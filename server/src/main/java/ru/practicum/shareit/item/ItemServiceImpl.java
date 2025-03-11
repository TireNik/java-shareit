package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Transactional
    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User owner = findUserById(userId);

        Item item = ItemMapper.toEntity(itemDto, owner);

        if (itemDto.getRequestId() != null) {
            item.setRequest(requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден")));
        }

        log.info("Сохраняем вещь: {} для пользователя {}", itemDto, owner.getName());
        itemRepository.save(item);
        return ItemMapper.toDto(item);
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Item item = findAndCheckItem(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Редактировать вещь может только её владелец");
        }

        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        itemRepository.save(item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = findAndCheckItem(itemId);

        BookingDtoOut lastBooking = bookingRepository
                .findTopByItemIdAndEndBeforeAndStatusOrderByEndDesc(itemId, LocalDateTime.now(), BookingStatus.APPROVED)
                .map(BookingMapper::toBookingOut)
                .orElse(null);

        BookingDtoOut nextBooking = bookingRepository
                .findTopByItemIdAndStartBeforeAndEndAfterOrderByStartAsc(itemId, LocalDateTime.now(), LocalDateTime.now())
                .map(BookingMapper::toBookingOut)
                .orElse(null);

        List<CommentDtoOut> comments = commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());

        log.info("lastBooking: {}", lastBooking);
        log.info("nextBooking: {}", nextBooking);

        return ItemMapper.toItemDtoOut(item, lastBooking, comments, nextBooking);
    }

    private Item findAndCheckItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        User owner = findUserById(userId);

        List<Item> items = itemRepository.findAllByOwner(owner);

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Booking> bookings = bookingRepository.findAllByItemIdInAndStatusApproved(itemIds);

        Map<Long, List<Booking>> bookingMap = bookings.stream()
                .collect(Collectors.groupingBy(g -> g.getItem().getId()));

        List<Comment> itemComments = commentRepository.findAllByItemIdIn(itemIds);

        Map<Long, List<CommentDtoOut>> commentMap = itemComments.stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toDto, Collectors.toList())));

        return items.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingMap.getOrDefault(item.getId(), Collections.emptyList());

                    Optional<Booking> lastBookingOpt = itemBookings.stream()
                            .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                            .reduce((first, second) -> second);

                    Optional<Booking> nextBookingOpt = itemBookings.stream()
                            .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                            .findFirst();

                    BookingDtoOut lastBooking = lastBookingOpt.map(BookingMapper::toBookingOut).orElse(null);
                    BookingDtoOut nextBooking = nextBookingOpt.map(BookingMapper::toBookingOut).orElse(null);
                    List<CommentDtoOut> comments = commentMap.getOrDefault(item.getId(), Collections.emptyList());

                    return ItemMapper.toItemDtoOut(item, lastBooking, comments, nextBooking);
                })
                .collect(Collectors.toList());
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    @Override
    public List<ItemDto> searchItems(String txt) {
        List<Item> items = itemRepository.searchItemsByTxt(txt.toLowerCase());
        return items.stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDtoOut createComment(Long userId, CommentDto dto, Long itemId) {
        User user = findUserById(userId);

        Item item = findAndCheckItem(itemId);

        List<Booking> userBookings = bookingRepository.findByBookerIdAndEndIsBefore(
                userId, LocalDateTime.now()
        );

        if (userBookings.isEmpty()) {
            throw new ValidationException("У пользователя с id " + userId +
                    " должно быть хотя бы одно бронирование предмета с id " + itemId);
        }

        Comment comment = CommentMapper.toEntity(dto, item, user);
        commentRepository.save(comment);
        log.info("Comment {}", comment);

        return CommentMapper.toDto(comment);
    }
}
