package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId);

    ItemDto getItemById(Long itemId);

    List<ItemDto> getItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String txt);

    CommentDtoOut createComment(Long userId, CommentDto dto, Long itemId);
}

