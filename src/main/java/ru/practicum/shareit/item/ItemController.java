package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collections;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(
            @RequestBody @Valid Item item,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Создание новой вещи: {}", item);
        ItemDto createdItem = itemService.createItem(item, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @PathVariable Long itemId,
            @RequestBody @Valid ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Редактирование вещи с ID {} пользователем с ID {}", itemId, userId);
        ItemDto updateItem = itemService.updateItem(itemId, itemDto, userId);
        return ResponseEntity.ok(updateItem);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получение информации о вещи с ID {} пользователем с ID {}", itemId, userId);
        ItemDto item = itemService.getItemById(itemId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItemsBuOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemDto> items = itemService.getItemsByOwner(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(
            @RequestParam String text,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Поиск вещей с текстом '{}' пользователем с ID {}", text, userId);
        if (text.isBlank()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<ItemDto> items = itemService.searchItems(text);
        return ResponseEntity.ok(items);
    }
}
