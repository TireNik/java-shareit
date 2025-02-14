package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

@UtilityClass
public class BookingMapper {
    public Booking toEntity(BookingDto dto, Item item, User user) {
        return new Booking(
                dto.getItemId(),
                item,
                user,
                dto.getStart(),
                dto.getEnd(),
                BookingStatus.WAITING
        );
    }

    public BookingDto toDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd()
        );
    }

    public BookingDtoOut toBookingOut (Booking booking) {
        return new BookingDtoOut(
                booking.getId(),
                ItemMapper.toDto(booking.getItem()),
                booking.getStart(),
                booking.getEnd(),
                UserMapper.toDto(booking.getUser()),
                booking.getStatus()
        );
    }
}
