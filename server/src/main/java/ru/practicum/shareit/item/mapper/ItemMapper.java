package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

@UtilityClass
public class ItemMapper {
    public ItemDto toDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        if (item.getRequest() != null) {
            itemDto.setRequestId(item.getRequest().getId());
        }
        return itemDto;
    }

    public Item toEntity(ItemDto itemDto, User owner) {
        Item item = new Item();
        item.setAvailable(itemDto.getAvailable());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setOwner(owner);
        return item;
    }

    public ItemDto toItemDtoOut(Item item, BookingDtoOut lastBooking, List<CommentDtoOut> comments, BookingDtoOut nextBooking) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(comments);
        if (item.getRequest() != null) {
            dto.setRequestId(item.getRequest().getId());
        }
        return dto;

    }
}
