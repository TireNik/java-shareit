package ru.practicum.shareit.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final ObjectMapper objectMapper;


    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository, ItemMapper itemMapper,
                           ObjectMapper objectMapper) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemMapper = itemMapper;
        this.objectMapper = objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с ID " + userId + " не найден");
        }
        Item item = itemMapper.toEntity(itemDto);
        item.setOwnerId(userId);

        log.info("Сохраняем вещь: {} для пользователя с ID {}", itemDto, userId);
        itemRepository.save(item);
        return itemMapper.toDto(item);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Item item = findAndCheckItem(itemId);

        if (!item.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Редактировать вещь может только её владелец");
        }

        try {
            objectMapper.updateValue(item, itemDto);
        } catch (JsonMappingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ошибка при обновлении данных вещи", e);
        }

        Item updateItem = itemRepository.save(item);
        return itemMapper.toDto(updateItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
       Item item = findAndCheckItem(itemId);
        return itemMapper.toDto(item);
    }

    private Item findAndCheckItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        return items.stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String txt) {
        List<Item> items = itemRepository.searchItemsByTxt(txt.toLowerCase());
        return items.stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

}
