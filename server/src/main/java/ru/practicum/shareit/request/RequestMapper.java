package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.util.List;

@UtilityClass
public class RequestMapper {
    public ItemRequest toEntity(ItemRequestDto dto, User user) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setUser(user);
        itemRequest.setCreated(dto.getCreated());
        itemRequest.setDescription(dto.getDescription());
        return itemRequest;
    }

    public ItemRequestDto toDto(ItemRequest itemRequest) {
        List<ItemDto> itemsDto = itemRequest.getItems().stream()
                .map(ItemMapper::toDto)
                .toList();

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(itemRequest.getId());
        itemRequestDto.setDescription(itemRequest.getDescription());
        itemRequestDto.setCreated(itemRequest.getCreated());
        itemRequestDto.setItems(itemsDto);

        return itemRequestDto;
    }
}
